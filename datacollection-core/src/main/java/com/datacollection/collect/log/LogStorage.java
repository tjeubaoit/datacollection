package com.datacollection.collect.log;

import com.google.common.util.concurrent.ListenableFuture;
import com.datacollection.collect.model.Log;
import com.datacollection.common.config.Properties;
import com.datacollection.common.utils.Reflects;

import java.io.Closeable;

/**
 * Đại diện cho layer làm việc với dữ liệu log
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public interface LogStorage extends Closeable {

    /**
     * Add log và lưu trữ vào storage
     *
     * @param uid uid của Profile sở hữu/liên quan tới log này
     * @param log Log object
     * @return Future mô tả trạng thái của thao tác add log
     */
    ListenableFuture<Log> addLog(String uid, Log log);

    /**
     * Tìm tất cả các log của một Pròile
     *
     * @param uid uid của Profile cần tìm kiếm log
     * @return Danh sách tất cả các logs của Profile tìm được
     */
    Iterable<Log> findLogByUid(String uid);

    @Override
    default void close() {
    }

    /**
     * Factory method khởi tạo một cài đặt cụ thể của LogStorage
     * bằng cách tạo ra một class mới từ tên class đọc được từ config
     *
     * @param p Properties chứa thuộc tính config tên class cài đặt LogStorage
     * @return một object mới cài đặt interface LogStorage
     */
    static LogStorage create(Properties p) {
        return Reflects.newInstance(p.getProperty("collect.log.storage.class"),
                new Class<?>[]{Properties.class}, p);
    }
}
