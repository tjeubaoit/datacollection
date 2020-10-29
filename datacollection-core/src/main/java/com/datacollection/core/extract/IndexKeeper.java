package com.datacollection.core.extract;

import com.datacollection.common.config.Properties;
import com.datacollection.common.io.FileHelper;
import com.datacollection.common.utils.DateTimes;
import com.datacollection.common.utils.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.Flushable;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Save/restore document index that processed by extractors
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
@SuppressWarnings("unchecked")
public class IndexKeeper implements Flushable {

    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final Logger logger = LoggerFactory.getLogger(IndexKeeper.class);

    private final File file;
    private final long delay;
    private final AtomicLong lastWrite = new AtomicLong(System.currentTimeMillis());
    private final Properties props = new Properties();

    /**
     * @param path            path to file where properties will write to or read from
     * @param delay           time in ms properties will be kept in buffer before
     *                        write to file on disk
     * @param lastLinesToLoad when load properties from file, read at least
     *                        <i>lastLinesToLoad</i> lines and load earliest value
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public IndexKeeper(String path, long delay, int lastLinesToLoad) {
        this.file = new File(path);
        this.delay = delay;
        if (file.isDirectory())
            throw new IllegalArgumentException("File must not be dir");

        try {
            if (!file.exists()) {
                String dir = file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf("/"));
                new File(dir).mkdirs();
            }
            this.reloadProperties(lastLinesToLoad);
        } catch (IOException e) {
            logger.warn(file.getAbsolutePath() + " not found, reset index");
        }
    }

    /**
     * Reload docIndex, docOrder from file
     */
    public void reloadProperties(int lastLinesToLoad) throws IOException {
        // read at least last lastLinesToLoad lines
        String[] lines = FileHelper.readLastLines(file, lastLinesToLoad);
        if (lines.length > 0) {
            String[] parts = lines[0].split("\t");
            for (String part : parts) {
                String[] pair = part.split("=");
                if (pair.length == 2) {
                    props.put(pair[0], pair[1]);
                }
            }
        }
    }

    /**
     * Persist properties to storage
     */
    public void persist() throws IOException {
        final long now = System.currentTimeMillis();
        if (now - lastWrite.get() < delay) return;
        this.lastWrite.set(now);
        this.doPersist();
    }

    @Override
    public void flush() {
        try {
            this.doPersist();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void doPersist() throws IOException {
        StringBuilder sb = new StringBuilder(DateTimes.format(new Date(), DATE_FORMAT));
        for (Map.Entry<String, Object> e : props.asMap().entrySet()) {
            sb.append(Strings.format("\t%s=%s", e.getKey(), e.getValue()));
        }

        FileHelper.checkCreateNewFile(file.getAbsolutePath());
        FileHelper.write(file.getAbsolutePath(), sb.toString(), true);
    }

    public IndexKeeper put(String key, Object value) {
        this.props.setProperty(key, value.toString());
        return this;
    }

    public String get(String key) {
        return this.props.getProperty(key);
    }
}
