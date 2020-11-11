package com.datacollection.extract;

import com.datacollection.common.config.Configuration;
import com.datacollection.common.utils.Strings;
import com.datacollection.common.utils.ThreadPool;
import com.datacollection.common.utils.Threads;
import com.datacollection.extract.model.GenericModel;

import java.util.concurrent.ExecutorService;

/**
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public abstract class StreamExtractor<TSource> extends Extractor {

    static final String KEY_POOL_SIZE = "extract.threadpool.core.size";
    static final String KEY_QUEUE_SIZE = "extract.threadpool.queue.size";

    private ExecutorService workerExecutor;

    public StreamExtractor(String group, Configuration config) {
        super(group, config);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        this.workerExecutor = ThreadPool.builder()
                .setCoreSize(props.getIntProperty(KEY_POOL_SIZE, 2))
                .setQueueSize(props.getIntProperty(KEY_QUEUE_SIZE, 512))
                .setDaemon(true)
                .setNamePrefix("extractor-worker")
                .build();
    }

    @Override
    protected void onLoop() {
        try (DataStream<TSource> stream = openDataStream()) {
            while (stream.hasNext() && isNotCanceled()) {
                TSource tSource = stream.next();
                try {
                    store(extractData(tSource), tSource);
                } catch (Throwable t) {
                    String s = Strings.firstCharacters(tSource.toString(), 500);
                    logger.warn("Extract data failed " + s, t);
                }
            }
        }
        if (isNotCanceled()) logger.info("All data processed");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Threads.stopThreadPool(workerExecutor);
    }

    protected abstract GenericModel extractData(TSource document);

    protected abstract DataStream<TSource> openDataStream();
}
