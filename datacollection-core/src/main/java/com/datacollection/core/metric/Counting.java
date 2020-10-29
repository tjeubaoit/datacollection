package com.datacollection.core.metric;

/**
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public interface Counting {

    long getCount();

    static Counting from(Number number) {
        return number::longValue;
    }
}
