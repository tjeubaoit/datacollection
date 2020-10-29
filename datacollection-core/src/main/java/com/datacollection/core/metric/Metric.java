package com.datacollection.core.metric;

import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class Metric {

    private final String group;
    private final String name;
    private final List<MetricProperty> properties = new LinkedList<>();

    public Metric(String group, String name) {
        this.group = group;
        this.name = name;
    }

    public String group() {
        return group;
    }

    public String name() {
        return name;
    }

    public List<MetricProperty> metricProperties() {
        return properties;
    }

    public void addProperty(MetricProperty property) {
        properties.add(property);
    }

    public void addProperty(String name, Number value) {
        properties.add(new MetricProperty(name, value));
    }

    @Override
    public String toString() {
        return name + ": " + properties;
    }
}
