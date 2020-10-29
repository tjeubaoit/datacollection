package com.datacollection.core.collect;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Futures;
import com.datacollection.core.collect.wal.WalException;
import com.datacollection.core.collect.wal.WalFile;
import com.datacollection.core.collect.wal.WalReader;
import com.datacollection.common.concurrency.AllInOneFuture;
import com.datacollection.common.config.Properties;
import com.datacollection.common.io.FileHelper;
import com.datacollection.common.serialize.Deserializer;
import com.datacollection.common.utils.Strings;
import com.datacollection.common.utils.Threads;
import com.datacollection.common.utils.Utils;
import com.datacollection.core.extract.model.GenericModel;
import com.datacollection.core.metric.Counter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.schedulers.Schedulers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Handler dùng để xử lý một file WAL trong WalCollector. Implement
 * lại interface Runnable nên có thể được submit vào một thread bất
 * kì để xử lý. Mỗi handler chỉ xử lý một file WAL tại một thời điểm
 * và file chỉ được coi là xử lý thành công nếu toàn bộ records trong
 * file đó được xử lý thành công, khi đó handler sẽ xóa file.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class WalCollectHandler implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final WalFile walFile;
    private final CollectService service;
    private final Deserializer<GenericModel> deserializer;
    private final Counter counter;

    private int total;
    private long startTime;
    private final String shortName;
    private final boolean asyncMode;
    private final int retries;

    /**
     * Init new WAL handler
     *
     * @param props        application properties
     * @param walFile      abstract layer of physical file that contains data to need to be processed
     * @param service      instance of collect service
     * @param deserializer used to deserialize data raw data to GenericModel object
     * @param counter      used for global metric to show number records was processed per second
     */
    WalCollectHandler(Properties props, WalFile walFile, CollectService service,
                      Deserializer<GenericModel> deserializer, Counter counter) {
        Preconditions.checkArgument(walFile.exists(), walFile.absolutePath() + " did not exists");

        this.walFile = walFile;
        this.service = service;
        this.deserializer = deserializer;
        this.counter = counter;

        this.shortName = FileHelper.getFileName(walFile.absolutePath());
        this.total = 0;
        this.asyncMode = props.getBoolProperty("wal.handler.async.mode", false);
        this.retries = props.getIntProperty("wal.handler.retries", 3);
    }

    @Override
    public void run() {
        this.startTime = System.currentTimeMillis();
        // Hai chế độ xử lý dữ liệu
        if (asyncMode) {
            collectAsync();
        } else {
            collectSync();
        }
    }

    /**
     * <p>Chế độ xử lý dữ liệu đồng bộ. Trong chế độ này các records sẽ được xử lý
     * tuần tự. Tại một thời điểm chỉ có một record được xử lý. Record sau chỉ được
     * xử lý nếu record trước đã xử lý xong. Nếu có lỗi xảy ra với một record sẽ
     * tìm cách retry lại với số lần quyết định bởi biến <i>retries</i>.
     * </p>
     * Nếu quá số lần retry mà không thể xử lý thành công một record cả file đó sẽ bị
     * coi là fail, hander sẽ kết thúc quá trình xử lý file hiện tại để nhường quyền
     * xử lý cho các file khác. File bị tính là fail sẽ không bị xóa mà sẽ được xử lý
     * lại tại một thời điểm khác.
     */
    private void collectSync() {
        try (WalReader reader = walFile.openForRead()) {
            byte[] data;
            // Xử lý lần lượt từng record
            while ((data = reader.next()) != null) {
                try {
                    for (int c = 0; ; ) {
                        try {
                            // Chờ cho record hiện tại được xử lý thành công (timeout 60s)
                            handleRecord(data).get(60, TimeUnit.SECONDS);
                            break; // Thoát khỏi vòng lặp xử lý record hiện tại nếu đã thành công
                        } catch (ExecutionException e) {
                            if (++c <= retries) { // thử lại nếu số lần thực thi nhỏ hơn retry
                                logger.warn(Strings.format("Process record error, retries = %d: %s",
                                        c, e.getCause().getMessage()));
                                Threads.sleep(500);
                            } else throw e.getCause(); // nếu quá số lần retry throw exception
                        }
                    }
                    total++;
                    counter.inc();
                } catch (Throwable e) {
                    logger.error("Process record error: " + rawDataToString(data), e);
                    throw new WalException(e);
                }
            }
            doOnSuccess();
        } catch (WalException e) {
            logger.warn(Utils.currentThreadName() + "Handle WAL error: " + shortName);
        }
    }

    /**
     * <p>Chế độ xử lý dữ liệu bất đồng bộ. Trong chế độ các records được xử lý đồng
     * thời thay vì tuần tự và phải chờ đợi nhau. Trạng thái xử lý của mỗi record
     * được quản lý bởi một Future. List các Future này sẽ được lưu lại và nếu tất
     * cả các Future này đều success, tất cả records được coi là xử lý thành công.
     * </p>
     * <b><i>Cần rất cẩn thận khi sử dụng chế độ này vì dễ dẫn tới OutOfMemoryError</b></i>
     */
    @SuppressWarnings("unchecked")
    private void collectAsync() {
        List<Future<?>> futures = new LinkedList<>();
        try (WalReader reader = walFile.openForRead()) {
            byte[] data;
            while ((data = reader.next()) != null) {
                // Không chờ record hiện tại được xử lý thành công mà add nó vào list
                // Futures và ngay lập tức xử lý records tiếp theo
                futures.add(handleRecord(data));
                total++;
                counter.inc();
            }

            // Sử dụng thư viện RxJava để quản lý các task ở background dễ hơn.
            // Toàn bộ records được coi là xử lý thành công nếu tất cả các Futures
            // success. Để xử lý các records đồng thời thì quá trình xử lý được thực
            // hiện trên threadpool quản lý bởi Scheduler IO của RxJava, đây là một
            // threadpool không có giới hạn vì thế rất dễ dẫn tới OutOfMemoryError
            rx.Observable.from(AllInOneFuture.from(futures))
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.computation())
                    .subscribe(o -> doOnSuccess());
        } catch (WalException e) {
            logger.warn(Utils.currentThreadName() + "Handle WAL error: " + shortName);
        }
    }

    private void doOnSuccess() {
        long took = System.currentTimeMillis() - startTime;
        if (walFile.delete()) { // Xóa file sau khi toàn bộ records được xử lý thành công
            logger.info(Utils.currentThreadName() + "Successfully handled WAL: " + shortName
                    + ", total records: " + total
                    + ", took: " + ((float) took / 1000));
        }
    }

    private Future<?> handleRecord(byte[] data) {
        GenericModel generic = deserialize(data);
        return generic != null ? service.collect(generic) : Futures.immediateFuture(0);
    }

    private GenericModel deserialize(byte[] data) {
        try {
            return deserializer.deserialize(data);
        } catch (IOException | NullPointerException e) {
            logger.warn("Deserialize record error", e);
        }
        return null;
    }

    private static String rawDataToString(byte[] bytes) {
        try {
            return new String(bytes, "utf-8");
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }
}
