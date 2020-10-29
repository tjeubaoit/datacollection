package com.datacollection.core.collect;

import com.datacollection.common.config.Configuration;
import com.datacollection.common.config.Properties;
import com.datacollection.common.mb.Record;
import com.datacollection.common.mb.Records;
import com.datacollection.common.utils.Threads;
import com.datacollection.core.extract.model.GenericModel;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * <p>Phiên bản cài đặt đơn giản của Collector, dùng cho các loại dữ liệu có số lượng
 * ít hoặc cho môi trường development. Quá trình xử lý data được thực hiện trực tiếp
 * ngay trên các thread của Message Broker. Mỗi record trong một MessageBroker cần
 * được xử lý thành công trước khi record tiếp theo có thể được xử lý. Nếu có lỗi xảy
 * ra với bất kì record nào sẽ retry lại liên tục. Chỉ sau khi một batch records được
 * xử lý xong mới được coi là hoàn thành. Nếu chương trình bị tắt trước khi một batch được
 * hoàn thành thì sẽ xử lý lại toàn bộ cả batch.
 * <p>
 * Ưu điểm của phương pháp này là đơn giản, dễ debug, test. Nhược điểm là số lượng worker
 * bị giới hạn bởi số reader của Message Broker (consumer nếu sử dụng Kafka) nên khó scale
 * khi muốn tăng tốc hoặc cần chạy lại nhiều dữ liệu.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class SimpleCollector extends Collector {

    public SimpleCollector(Properties props) {
        super(props);
    }

    @Override
    public void handle(Records records) {
        for (Record record : records) {
            while (isNotCanceled()) {
                try {
                    GenericModel generic = getDeserializer().deserialize(record.data());
                    if (generic != null) getService().collect(generic).get(60, TimeUnit.SECONDS);
                    break;
                } catch (IOException e) {
                    logger.warn("Deserialize record error", e);
                } catch (Exception e) {
                    logger.error("Process record error: " + new String(record.data()), e);
                    Threads.sleep(TimeUnit.SECONDS.toMillis(5));
                }
            }
            counter.inc();
        }
    }

    public static void main(String[] args) {
        Properties p = new Configuration().toSubProperties("collect", "SimpleCollector");
        new SimpleCollector(p).start();
    }
}
