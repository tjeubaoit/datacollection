package com.datacollection.common.utils;

/**
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public interface Converter<S, R> {

    R convert(S source);
}
