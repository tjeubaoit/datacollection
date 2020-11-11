package com.datacollection.collect.wal;

import com.datacollection.common.types.IdGenerator;
import com.datacollection.common.types.SequenceIdGenerator;

/**
 * Interface biểu diễn cho một đối tượng file WAL
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public interface WalFile {

    /**
     * @return Đường dẫn tuyệt đối của File, có thể bao gồm cả URI
     */
    String absolutePath();

    /**
     * @return true nếu file tồn tại và false nếu không tồn tại
     */
    boolean exists();

    /**
     * @return true nếu file có độ lớn đã đạt tới giới hạn và falsen nếu chưa
     */
    boolean isReachedLimit();

    /**
     * Mở file để đọc
     *
     * @return đối tượng WalReader dùng để đọc file
     */
    WalReader openForRead();

    /**
     * Mở file để ghi
     *
     * @return đối tượng WalWriter dùng để ghi file
     */
    WalWriter openForWrite();

    /**
     * Xóa file
     *
     * @return true nếu xóa file thành công hoặc false nếu có lỗi xảy ra
     */
    boolean delete();

    /**
     * Dùng để generate suffix cho tên file WAL, dùng để tạo
     * tên file mới không trùng với bất kì file nào đang có
     */
    IdGenerator ID_GENERATOR = new SequenceIdGenerator();

    /**
     * @return new WAL file name
     */
    static String newFileName() {
        return "wal-" + ID_GENERATOR.generate() + ".log";
    }

    /**
     * Tạo đối tượng WalWriter từ một file WAL và codec cụ thể
     *
     * @param file  WAL file cần ghi
     * @param codec codec của file WAl
     * @return đối tượng WalWriter dùng để ghi file
     */
    static WalWriter getWriter(WalFile file, String codec) {
        switch (codec) {
            case "simple":
                return new SimpleWriter(file);
            default:
                throw new IllegalArgumentException("Invalid WAL file codec");
        }
    }

    /**
     * Tạo đối tượng WalReader từ một file WAL và codec cụ thể
     *
     * @param file  WAL file cần đọc
     * @param codec codec của file WAl
     * @return đối tượng WalReader dùng để đọc file
     */
    static WalReader getReader(WalFile file, String codec) {
        switch (codec) {
            case "simple":
                return new SimpleReader(file);
            default:
                throw new IllegalArgumentException("Invalid WAL file codec");
        }
    }
}
