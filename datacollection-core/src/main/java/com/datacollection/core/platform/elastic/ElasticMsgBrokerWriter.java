package com.datacollection.core.platform.elastic;

import com.google.common.util.concurrent.Futures;
import com.datacollection.common.config.Properties;
import com.datacollection.common.mb.MsgBrokerWriter;
import com.datacollection.common.serialize.Deserializer;
import com.datacollection.common.serialize.Serialization;
import com.datacollection.common.utils.Threads;
import com.datacollection.common.utils.Utils;
import com.datacollection.core.extract.model.GenericModel;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class ElasticMsgBrokerWriter implements MsgBrokerWriter {

    private String esIndex;
    private Client esClient;
    private BulkRequestBuilder bulk;
    private int bulkSize;
    private Deserializer<GenericModel> deserializer;

    private final AtomicLong lastActivate = new AtomicLong(System.currentTimeMillis());
    private final AtomicBoolean scheduledExecutorRunning = new AtomicBoolean(false);
    private final AtomicLong counter = new AtomicLong();
    private final List<CompletableFuture<Long>> pendingFutures = new ArrayList<>(500);

    private static final Logger logger = LoggerFactory.getLogger(ElasticMsgBrokerWriter.class);

    @Override
    public Future<Long> write(byte[] b) {
        try {
            while (scheduledExecutorRunning.get()) {
                // wait for scheduled executor's task finished
                logger.warn("Scheduled executor is running");
                Threads.sleep(2, TimeUnit.SECONDS);
            }

            lastActivate.set(System.currentTimeMillis());
            GenericModel genericModel = deserializer.deserialize(b);

            bulk.add(new IndexRequest(esIndex, "org", genericModel.getId()).source(b, XContentType.JSON));
            if (bulk.numberOfActions() >= bulkSize) {
                return submitBulk();
            }

            CompletableFuture<Long> fut = new CompletableFuture<>();
            pendingFutures.add(fut);
            return fut;
        } catch (Exception e) {
            return Futures.immediateFailedFuture(e);
        }
    }

    @Override
    public void configure(Properties p) {
        ElasticConfig elasticConfig = new ElasticConfig(p);
        esIndex = elasticConfig.getElasticIndex();
        esClient = ElasticClientProvider.getDefault(elasticConfig);
        bulk = esClient.prepareBulk();
        bulkSize = p.getIntProperty("elastic.bulk.size", 100);
        deserializer = Serialization.create(p.getProperty("mb.serializer"), GenericModel.class).deserializer();

        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
        ses.scheduleAtFixedRate(() -> {
            scheduledExecutorRunning.set(true);
            final long duration = System.currentTimeMillis() - lastActivate.get();
            if (TimeUnit.MILLISECONDS.toSeconds(duration) > 30 && bulk.numberOfActions() >= 0) {
                logger.info("Do submit bulk in background");
                submitBulk();
            }
            scheduledExecutorRunning.set(false);
        }, 10,60, TimeUnit.SECONDS);

        Utils.addShutdownHook(ses::shutdown);
    }

    private synchronized Future<Long> submitBulk() {
        try {
            BulkResponse resp = bulk.get();
            if (resp.hasFailures())
                throw new ElasticsearchException(resp.buildFailureMessage());

            pendingFutures.forEach(fut -> fut.complete(counter.incrementAndGet()));
            return Futures.immediateFuture(counter.incrementAndGet());
        } catch (RuntimeException ex) {
            pendingFutures.forEach(fut -> fut.completeExceptionally(ex));
            return Futures.immediateFailedFuture(ex);
        } finally {
            bulk = esClient.prepareBulk();
            pendingFutures.clear();
        }
    }
}
