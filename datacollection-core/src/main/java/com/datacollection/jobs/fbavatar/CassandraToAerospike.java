package com.datacollection.jobs.fbavatar;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.Bin;
import com.aerospike.client.Key;
import com.aerospike.client.policy.WritePolicy;
import com.datacollection.platform.cassandra.CassandraClusterProvider;
import com.datacollection.platform.cassandra.CassandraConfig;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datacollection.common.config.Configuration;
import com.datacollection.common.config.Properties;
import com.datacollection.common.io.FileHelper;
import com.datacollection.common.lifecycle.AbstractLifeCycle;
import com.datacollection.common.utils.ThreadPool;
import com.datacollection.common.utils.Threads;
import com.datacollection.metric.Counter;
import com.datacollection.metric.CounterMetrics;
import com.datacollection.metric.Sl4jPublisher;
import com.datacollection.platform.aerospike.AerospikeClientProvider;
import com.datacollection.platform.aerospike.AerospikeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class CassandraToAerospike extends AbstractLifeCycle {

    private static final Logger logger = LoggerFactory.getLogger(CassandraToAerospike.class);
    private final Properties props;
    private Session session;
    private Counter counter = new Counter();
    private CounterMetrics counterMetrics;
    private ExecutorService executor;

    private AerospikeClient aerospikeClient;
    private String aerospikeNamespace;
    private int numThread;

    public CassandraToAerospike(Properties props) {
        this.props = props;
    }

    @Override
    protected void onInitialize() {
        session = CassandraClusterProvider.getDefault(new CassandraConfig(props)).connect("datacollection");

        AerospikeConfig aerospikeConf = new AerospikeConfig(props);
        aerospikeClient = AerospikeClientProvider.getDefault(aerospikeConf);
        aerospikeNamespace = aerospikeConf.getNamespace();

        this.numThread = props.getIntProperty("number.of.threads", Runtime.getRuntime().availableProcessors());

    }

    @Override
    protected void onStart() {
        List<Long> tokens = loadSortedTokens();
        executor = ThreadPool.builder()
                .setCoreSize(this.numThread)
                .setQueueSize(5)
                .setDaemon(true)
                .setNamePrefix("sync-aerospike-worker")
                .build();
        int tokenLength = tokens.size() / this.numThread;
        AtomicLong fromToken = new AtomicLong(Long.MIN_VALUE);
        int indexTk = 0;
        for (int th = 1; th <= this.numThread; th++) {
            long fromTk = fromToken.get();
            indexTk += tokenLength - 1;
            long toTk = th == this.numThread ? Long.MAX_VALUE : tokens.get(indexTk);
            executor.submit(() -> process(fromTk, toTk));
            fromToken.set(toTk);
        }

        counterMetrics = new CounterMetrics(new Sl4jPublisher(), "fbavatar",
                "cassandra-to-aerospike", counter, 1000);
        counterMetrics.start();
    }

    @Override
    protected void onStop() {
        counterMetrics.stop();
        logger.info("Stopping worker threads...");

        Threads.stopThreadPool(executor, 30, TimeUnit.SECONDS);
        session.close();
        logger.info("All worker threads stopped");

        aerospikeClient.close();
    }

    private List<Long> loadSortedTokens() {
        Row row = session.execute("SELECT tokens FROM system.local").one();
        Set<String> tokens = row.getSet("tokens", String.class);
        List<Long> sortedTokens = new ArrayList<>(tokens.size());
        for (String tk : tokens) {
            sortedTokens.add(Long.parseLong(tk));
        }
        Collections.sort(sortedTokens);
        return sortedTokens;
    }

    private void process(long from, long to) {
        String indexPath = getIndexFileFromTokenRange(from, to);
        from = loadLastToken(indexPath, from);
        logger.info(String.format(Locale.US, "Loaded last token from %s: %d", indexPath, from));
        while (true) {
            try {
                String query = "SELECT token(id) as tk, id, url FROM fbavatar WHERE token(id) >= ? AND token(id) < ?";
                long lastWriteIndex = System.currentTimeMillis();

                for (Row row : session.execute(query, from, to)) {
                    if (isCanceled()) break;
                    long lastToken = row.getLong("tk");
                    String id = row.getString("id");
                    String url = row.getString("url");

                    handleOne(id, url);

                    long now = System.currentTimeMillis();
                    if (now - lastWriteIndex > 15000) {
                        lastWriteIndex = now;
                        saveIndex(indexPath, lastToken);
                    }
                }
                if (isNotCanceled()) logger.info("All records processed in range: " + from + " - " + to);
                break;
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
    }

    private String getIndexFileFromTokenRange(long from, long to) {
        String dataDir = System.getProperty("data.path");
        if (dataDir == null) dataDir = props.getProperty("data.path");
        return dataDir + "/" + from + "_" + to + ".index";
    }

    private void handleOne(String id, String url) {
        counter.inc();
        Key key = new Key(aerospikeNamespace, "fbavatar1", id);
        Bin bin = new Bin("url", url);
        WritePolicy policy = new WritePolicy(aerospikeClient.writePolicyDefault);
//        policy.generationPolicy = GenerationPolicy.EXPECT_GEN_EQUAL;
        aerospikeClient.put(policy, key, bin);
    }

    private static void saveIndex(String path, long token) {
        try {
            FileHelper.checkCreateNewFile(path);
            FileHelper.write(path, String.valueOf(token), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static long loadLastToken(String path, long defVal) {
        try {
            String s = FileHelper.readLastLine(new File(path)).trim();
            return Long.parseLong(s);
        } catch (Throwable t) {
            return defVal;
        }
    }

    public static void main(String[] args) {
        Properties p = new Configuration().toSubProperties("sync_aerospike");
        new CassandraToAerospike(p).start();
    }
}
