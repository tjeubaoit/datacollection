package com.datacollection.collect;

import com.datacollection.collect.wal.DefaultWalFile;
import com.datacollection.collect.wal.WalFile;
import com.datacollection.collect.wal.WalWriter;
import com.datacollection.common.concurrency.AllInOneFuture;
import com.datacollection.common.config.Configuration;
import com.datacollection.common.config.Properties;
import com.datacollection.common.io.FileHelper;
import com.datacollection.core.Record;
import com.datacollection.core.Records;
import com.datacollection.common.tasks.TaskErrorExceedLimitException;
import com.datacollection.common.tasks.TaskManager;
import com.datacollection.common.utils.ThreadPool;
import com.datacollection.common.utils.Threads;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * <p>Lấy ý tưởng từ file WAL của HBase, WalCollector thay vì xử lý data trực tiếp
 * trong các thread của consumer thì sẽ tạo ra một file đặc biệt gọi là WAL
 * (write-ahead-log). Record được write thành công vào file (append) sẽ được coi
 * là thành công. Ngay sau khi write thành công một batch vào file batch đó sẽ được
 * coi là hoàn thành ngay mà không cần quan tâm dữ liệu đó đã được xử lý thành công
 * hay chưa. Vì không xử lý ngay trong các consumer thread nên số thread cần sẽ ít
 * hơn so với SimpleCollector.
 * </p>
 * Các workers ở background sẽ đọc các file WAL ở trên để xử lý. Số lượng workers
 * này có thể nhiều hay ít tùy thuộc vào khả năng xử lý của server và không bị ảnh
 * hưởng bởi số lượng partition như phương pháp trước. Sau khi toàn bộ các records
 * trong một file được xử lý thành công, worker xử lý file đó sẽ xóa nó đi.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class WalCollector extends Collector {

    public static final long WAL_SIZE_LIMIT_IN_BYTES = 1024 * 1024; // 1 MB
    public static final String WAL_CODEC_SIMPLE = "simple";

    private final ThreadLocal<WalFile> walFileThreadLocal = new ThreadLocal<>();
    private String walCodec;
    private long walSizeLimit;
    private String walDataDir;
    private TaskManager taskManager;

    public WalCollector(Properties props) {
        super(props);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        // init collect executors
        ExecutorService executor = ThreadPool.builder()
                .setCoreSize(props.getIntProperty("threadpool.core.size",
                        Runtime.getRuntime().availableProcessors()))
                .setQueueSize(props.getIntProperty("threadpool.queue.size", 4))
                .setNamePrefix("collector-worker")
                .setDaemon(true)
                .build();
        taskManager = new TaskManager(props, executor);

        // init wal properties
        walCodec = props.getProperty("wal.codec", WAL_CODEC_SIMPLE);
        walSizeLimit = props.getLongProperty("wal.size.limit", WAL_SIZE_LIMIT_IN_BYTES);
        walDataDir = props.getProperty("data.path") + "/wal/";
        FileHelper.checkCreateDir(walDataDir);
    }

    @Override
    public void onStart() {
        processUnhandledWalFiles();
        super.onStart();
    }

    @Override
    public void onStop() {
        logger.info("Wait for all workers finish before stopped...");
        Threads.stopThreadPool(taskManager.executor());
        super.onStop();
    }

    @Override
    public void handle(Records records) {
        WalFile wal = walFileThreadLocal.get();
        if (wal == null) {
            wal = createNewWalFile();
            walFileThreadLocal.set(wal);
        }

        try (WalWriter writer = wal.openForWrite()) {
            for (Record record : records) {
                writer.append(record.data());
            }
        }
        if (!wal.isReachedLimit()) return;

        Runnable task = newWalExecutor(wal);
        while (isNotCanceled()) {
            try {
                if (taskManager.trySubmit(task) != null) {
                    walFileThreadLocal.set(createNewWalFile());
                    break;
                }
                Threads.sleep(5);
            } catch (TaskErrorExceedLimitException e) {
                logger.error(e.getMessage(), e);
                Threads.sleep(2000);
                taskManager.reset();
            }
        }
    }

    private WalFile createNewWalFile() {
        String path = walDataDir + WalFile.newFileName();
        WalFile wal = new DefaultWalFile(path, walCodec, walSizeLimit);
        walFileThreadLocal.set(wal);
        return wal;
    }

    @SuppressWarnings("ConstantConditions")
    private void processUnhandledWalFiles() {
        File dir = new File(walDataDir);
        if (dir.list() == null || dir.list().length == 0) {
            logger.info("No unhandled wal files");
            return;
        }

        logger.info(dir.list().length + " wal files need to process");
        List<Future<?>> futures = new ArrayList<>(dir.list().length);

        for (String file : dir.list()) {
            WalFile wal = new DefaultWalFile(walDataDir + file, walCodec, walSizeLimit);
            Runnable task = newWalExecutor(wal);
            while (isNotCanceled()) {
                Future<?> fut = taskManager.trySubmit(task);
                if (fut != null) {
                    futures.add(fut);
                    break;
                }
                Threads.sleep(500);
            }
        }

        Future<?> fut = AllInOneFuture.from(futures);
        while (!fut.isDone() && isNotCanceled()) {
            Threads.sleep(500);
        }
    }

    private WalCollectHandler newWalExecutor(WalFile wal) {
        return new WalCollectHandler(props, wal, getService(), getDeserializer(), counter);
    }

    public static void main(String[] args) {
        Properties p = new Configuration().toSubProperties("collect", "WalCollector");
        new WalCollector(p).start();
    }
}
