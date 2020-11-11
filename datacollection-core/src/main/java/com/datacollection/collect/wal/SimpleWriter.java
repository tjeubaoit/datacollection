package com.datacollection.collect.wal;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Dùng để write vào file WAL với codec simple
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class SimpleWriter implements WalWriter {

    private final BufferedWriter writer;

    /**
     * Create new SimpleWriter object
     *
     * @param file WAL file
     */
    public SimpleWriter(WalFile file) {
        try {
            this.writer = new BufferedWriter(new FileWriter(file.absolutePath(), true));
        } catch (IOException e) {
            throw new WalException(e);
        }
    }

    @Override
    public void append(byte[] data) {
        try {
            this.writer.write(new String(data, "utf-8") + "\n");
        } catch (IOException e) {
            throw new WalException(e);
        }
    }

    @Override
    public void close() {
        try {
            this.writer.close();
        } catch (IOException e) {
            throw new WalException(e);
        }
    }
}
