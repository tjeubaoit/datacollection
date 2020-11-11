package com.datacollection.metric;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class CounterMetrics {

    private final MetricPublisher publisher;
    private final String metricGroup;
    private final String metricName;
    private final Counting counting;
    private final long period;
    private final ScheduledExecutorService executor;

    public CounterMetrics(MetricPublisher publisher, String metricGroup, String metricName,
                          Counting counting, long periodInMs) {
        this.publisher = publisher;
        this.metricGroup = metricGroup;
        this.metricName = metricName;
        this.counting = counting;
        this.period = periodInMs;
        this.executor = Executors.newSingleThreadScheduledExecutor();
    }

    public void start() {
        executor.scheduleAtFixedRate(new Runnable() {
            long lastVal = counting.getCount();

            @Override
            public void run() {
                long total = counting.getCount();
                long val = total - lastVal;
                lastVal = total;

                Metric metric = new Metric(metricGroup, metricName);
                metric.addProperty("Records/s", val);
                metric.addProperty("Total", total);
                publisher.addMetric(metric);
            }
        }, 0, period, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        executor.shutdown();
    }
}
