package com.datacollection.metric;

/**
 * Quy định cách metrics được publish
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public interface MetricPublisher {

    void addMetric(Metric metric);
}
