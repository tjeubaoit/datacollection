package com.datacollection.core.collect.wal;

import java.io.File;

/**
 * Cài đặt dạng mặc định của file WAL, lưu trữ dưới dạng một
 * file thông thường trên local File System.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class DefaultWalFile implements WalFile {

    private final File file;
    private final String codec;
    private final long maxSizeInBytes;

    /**
     * Tạo một đối tượng DefaultWalFile mới
     *
     * @param path           đường dẫn tuyệt đối của File
     * @param codec          codec dùng để tạo các đối tượng đọc ghi file
     * @param maxSizeInBytes số bytes quy định độ lớn tối đa của file
     */
    public DefaultWalFile(String path, String codec, long maxSizeInBytes) {
        this.file = new File(path);
        this.codec = codec;
        this.maxSizeInBytes = maxSizeInBytes;
    }

    @Override
    public String absolutePath() {
        return this.file.getAbsolutePath();
    }

    @Override
    public boolean exists() {
        return this.file.exists();
    }

    @Override
    public boolean isReachedLimit() {
        return this.file.length() > maxSizeInBytes;
    }

    @Override
    public WalReader openForRead() {
        return WalFile.getReader(this, codec);
    }

    @Override
    public WalWriter openForWrite() {
        return WalFile.getWriter(this, codec);
    }

    @Override
    public boolean delete() {
        return this.file.delete();
    }
}
