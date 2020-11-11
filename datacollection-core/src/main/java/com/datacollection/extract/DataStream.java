package com.datacollection.extract;

import java.io.Closeable;
import java.util.Iterator;

/**
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public interface DataStream<T> extends Closeable, Iterator<T> {

    boolean hasNext();

    T next();

    void close();
}
