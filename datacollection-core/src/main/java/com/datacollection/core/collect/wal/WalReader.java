package com.datacollection.core.collect.wal;

import java.io.Closeable;

/**
 * Dùng để đọc dữ liệu từ file WAL, cài đặt cụ thể tùy thuộc vào
 * codec của file và do các class implement lại interface này quyết định
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public interface WalReader extends Closeable {

    /**
     * Đọc block dữ liệu tiếp theo
     *
     * @return block dữ liệu tiếp theo hoặc null nếu đã đọc tới cuối file
     */
    byte[] next();

    @Override
    void close();
}
