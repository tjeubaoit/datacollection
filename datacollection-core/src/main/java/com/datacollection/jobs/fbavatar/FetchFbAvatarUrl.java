package com.datacollection.jobs.fbavatar;

import com.datacollection.platform.cassandra.CassandraClusterProvider;
import com.datacollection.platform.cassandra.CassandraConfig;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datacollection.common.FacebookClient;
import com.datacollection.common.config.Configuration;
import com.datacollection.common.config.Properties;
import com.datacollection.common.io.FileHelper;
import com.datacollection.common.utils.ThreadPool;
import com.datacollection.common.utils.Threads;
import com.datacollection.common.utils.Utils;
import com.datacollection.metric.CounterMetrics;
import com.datacollection.metric.Counting;
import com.datacollection.metric.Sl4jPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class FetchFbAvatarUrl {

    static final Logger logger = LoggerFactory.getLogger(FetchFbAvatarUrl.class);
    static Properties props;
    static Session session;
    static PreparedStatement ps;
    static AtomicLong counter = new AtomicLong();
    static AtomicBoolean flagStop = new AtomicBoolean(false);

    public static void main(String[] args) {
        props = new Configuration().toSubProperties("fbavatar");
        session = CassandraClusterProvider.getDefault(new CassandraConfig(props)).connect();
        ps = session.prepare("INSERT INTO datacollection.fbavatar (id, url) VALUES (?, ?)");

        List<Long> tokens = loadSortedTokens();
        ExecutorService executor = ThreadPool.builder()
                .setCoreSize(tokens.size())
                .setQueueSize(5)
                .setDaemon(true)
                .setNamePrefix("fbavatar-fetcher-pool")
                .build();

        AtomicLong fromToken = new AtomicLong(Long.MIN_VALUE);
        for (long tk : tokens) {
            long from = fromToken.get();
            executor.submit(() -> process(from, tk));
            fromToken.set(tk);
        }

        CounterMetrics counterMetrics = new CounterMetrics(new Sl4jPublisher(), "fbavatar",
                "FetchFbAvatar", Counting.from(counter), 1000);
        counterMetrics.start();

        Utils.addShutdownHook(() -> {
            flagStop.set(true);
            counterMetrics.stop();

            logger.info("Stopping worker threads...");
            Threads.stopThreadPool(executor, 30, TimeUnit.SECONDS);
            session.close();
            logger.info("All worker threads stopped");
        });
    }

    static List<Long> loadSortedTokens() {
        Row row = session.execute("SELECT tokens FROM system.local").one();
        Set<String> tokens = row.getSet("tokens", String.class);
        List<Long> sortedTokens = new ArrayList<>(tokens.size());
        for (String tk : tokens) {
            sortedTokens.add(Long.parseLong(tk));
        }
        Collections.sort(sortedTokens);
        return sortedTokens;
    }

    static void process(long from, long to) {
        String indexPath = getIndexFileFromTokenRange(from, to);
        from = loadLastToken(indexPath, from);
        logger.info(String.format(Locale.US, "Loaded last token from %s: %d", indexPath, from));

        String query = "SELECT token(guid) as tk, facebook " +
                "FROM datacollection1.profiles " +
                "WHERE token(guid) >= ? AND token(guid) < ?";
        long lastWriteIndex = System.currentTimeMillis();

        for (Row row : session.execute(query, from, to)) {
            if (flagStop.get()) break;
            long lastToken = row.getLong("tk");
            Map<String, String> map = row.getMap("facebook", String.class, String.class);
            handleOne(map);

            long now = System.currentTimeMillis();
            if (now - lastWriteIndex > 15000) {
                lastWriteIndex = now;
                saveIndex(indexPath, lastToken);
            }
        }
        if (!flagStop.get()) logger.info("All records processed in range: " + from + " - " + to);
    }

    static String getIndexFileFromTokenRange(long from, long to) {
        String dataDir = System.getProperty("data.path");
        if (dataDir == null) dataDir = props.getProperty("data.path");
        return dataDir + "/" + from + "_" + to + ".index";
    }

    static void handleOne(Map<String, String> map) {
        Map<String, String> cache = new TreeMap<>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            try {
                String url;
                if (cache.containsKey(entry.getValue())) {
                    url = cache.get(entry.getValue());
                } else {
                    url = loadCache(entry.getKey());
                    if (url == null) url = FacebookClient.fetchAvatarUrl(entry.getKey());
                    if (url != null) {
                        counter.incrementAndGet();
                        cache.put(entry.getValue(), url);
                    } else continue;
                }

                session.executeAsync(ps.bind(entry.getKey(), url));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    static String loadCache(String id) {
        Row row = session.execute("SELECT url FROM datacollection1.fbavatar WHERE id = ?", id).one();
        return row != null ? row.getString("url") : null;
    }

    static void saveIndex(String path, long token) {
        try {
            FileHelper.checkCreateNewFile(path);
            FileHelper.write(path, String.valueOf(token), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static long loadLastToken(String path, long defVal) {
        try {
            String s = FileHelper.readLastLine(new File(path)).trim();
            return Long.parseLong(s);
        } catch (Throwable t) {
            return defVal;
        }
    }
}
