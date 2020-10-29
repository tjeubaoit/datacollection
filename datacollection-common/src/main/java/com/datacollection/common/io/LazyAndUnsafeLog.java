package com.datacollection.common.io;

import com.datacollection.common.config.Properties;
import com.datacollection.common.utils.Strings;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Dùng để ghi log, hỗ trợ lazy write (delay write trong một khoảng thời
 * gian trước khi thực hiện), quá trình write chấp nhận lỗi xảy ra và
 * không có exception được throws (unsafe).
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class LazyAndUnsafeLog {

    private final String path;
    private final long delay;
    private final Collection<String> buffer = new ConcurrentLinkedQueue<>();
    private final AtomicLong lastWrite = new AtomicLong(System.currentTimeMillis());

    public LazyAndUnsafeLog(Properties p, String fileName) {
        this(p.getProperty("logging.path") + "/" + fileName,
                p.getLongProperty("logging.lazy.delay.ms", 500));
    }

    public LazyAndUnsafeLog(Properties p, String fileName, long delay) {
        this(p.getProperty("logging.path") + "/" + fileName, delay);
    }

    public LazyAndUnsafeLog(String path, long delay) {
        this.path = path;
        this.delay = delay;
    }

    public String path() {
        return path;
    }

    public long delay() {
        return delay;
    }

    public void flush() {
        this.doWrite();
    }

    public void write(String msg) {
        buffer.add(msg);
        this.checkWrite();
    }

    public void write(String msg, Throwable t) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(8192);
        PrintStream stream = new PrintStream(bos);
        t.printStackTrace(stream);

        byte[] bytes = bos.toByteArray();
        buffer.add(msg + "\n" + new String(bytes));

        this.checkWrite();
    }

    private synchronized void checkWrite() {
        if (System.currentTimeMillis() - lastWrite.get() > delay) {
            this.doWrite();
        }
    }

    private synchronized void doWrite() {
        try {
            FileHelper.checkCreateNewFile(path);
            String log = Strings.join(buffer, "\n");
            FileHelper.unsafeWrite(path, log);
        } finally {
            lastWrite.set(System.currentTimeMillis());
            buffer.clear();
        }
    }
}
