package com.datacollection.core.metric;

/**
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class MetricProperty {

    private final String name;
    private final Number value;

    public MetricProperty(String name, Number value) {
        this.name = name;
        this.value = value;
    }

    public String name() {
        return name;
    }

    public Number value() {
        return value;
    }

    @Override
    public String toString() {
        return name + " = " + value;
    }
}
