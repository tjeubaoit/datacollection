package com.datacollection.core.extract;

import java.io.Closeable;
import java.util.Iterator;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public interface DataStream<T> extends Closeable, Iterator<T> {

    boolean hasNext();

    T next();

    void close();
}
