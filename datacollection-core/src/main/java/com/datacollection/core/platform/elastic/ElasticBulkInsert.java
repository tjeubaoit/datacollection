package com.datacollection.core.platform.elastic;

import com.datacollection.common.config.Properties;
import com.datacollection.common.utils.Threads;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.Flushable;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class ElasticBulkInsert implements Closeable, Flushable {

    private static final Logger logger = LoggerFactory.getLogger(ElasticBulkInsert.class);
    private final ThreadLocal<BulkRequestBuilder> requestBuilderThreadLocal = new ThreadLocal<>();
    private final Map<String, BulkRequestBuilder> requestBuilderMap = new HashMap<>();
    private final Client client;
    private final String index;
    private final int retries;

    public ElasticBulkInsert(Properties props) {
        final ElasticConfig esConfig = new ElasticConfig(props);
        this.client = ElasticClientProvider.getDefault(esConfig);
        this.index = esConfig.getElasticIndex();
        this.retries = props.getIntProperty("elastic.index.retries", 0);
    }

    /**
     * Add a request to local thread bulk request builder
     *
     * @param type   ES type
     * @param id     ES document id
     * @param source source to be indexed
     */
    public void addRequest(String type, String id, Map source) {
        BulkRequestBuilder requestBuilder = requestBuilderThreadLocal.get();
        if (requestBuilder == null) {
            requestBuilder = client.prepareBulk();
            requestBuilderThreadLocal.set(requestBuilder);
        }

        IndexRequest indexRequestParent = new IndexRequest(this.index, type, id).source(source);
        requestBuilder.add(indexRequestParent);
    }

    @Deprecated
    public void addRequest(String type, String id, String source) {
        BulkRequestBuilder requestBuilder = requestBuilderThreadLocal.get();
        if (requestBuilder == null) {
            requestBuilder = prepareNewBulk();
        }

        IndexRequest indexRequestParent = new IndexRequest(this.index, type, id).source(source);
        requestBuilder.add(indexRequestParent);
    }

    public ListenableActionFuture<BulkResponse> submitBulkAsync() {
        BulkRequestBuilder requestBuilder = requestBuilderThreadLocal.get();
        prepareNewBulk();
        return requestBuilder.execute();
    }

    public BulkResponse submitBulk() {
        BulkRequestBuilder requestBuilder = requestBuilderThreadLocal.get();
        BulkResponse response = requestBuilder.get();

        int retry = 0;
        while (retry++ < retries && response.hasFailures()) {
            response = requestBuilder.get();
            Threads.sleep(500);
        }
        if (response.hasFailures()) {
            throw new ElasticsearchException("Bulk request error: " + response.buildFailureMessage());
        }

        prepareNewBulk();
        return response;
    }

    /**
     * @return Current thread local bulk size
     */
    public int bulkSize() {
        BulkRequestBuilder requestBuilder = requestBuilderThreadLocal.get();
        return (requestBuilder == null || requestBuilder.numberOfActions() == 0)
                ? 0 : requestBuilder.numberOfActions();
    }

    public Client client() {
        return this.client;
    }

    /**
     * Flush all pending bulk request builder
     */
    @Override
    public void flush() {
        logger.info("Flush all pending BulkRequestBuilders...");
        requestBuilderMap.forEach((name, requestBuilder) -> {
            if (requestBuilder.numberOfActions() == 0) return;
            logger.info("Flush BulkRequestBuilder at thread: " + name);
            requestBuilder.get();
        });
        // clear all after flush
        requestBuilderMap.clear();
    }

    private BulkRequestBuilder prepareNewBulk() {
        BulkRequestBuilder requestBuilder = client.prepareBulk();
        requestBuilderMap.put(Thread.currentThread().getName(), requestBuilder);
        requestBuilderThreadLocal.set(requestBuilder);
        return requestBuilder;
    }

    @Override
    public void close() {
        this.client.close();
    }
}
