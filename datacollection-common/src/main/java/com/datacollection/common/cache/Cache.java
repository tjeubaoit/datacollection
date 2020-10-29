package com.datacollection.common.cache;

/**
 * Simple Cache interface
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public interface Cache<K, V> {
    V get(K var1);

    void put(K var1, V var2);

    boolean remove(K var1);

    long size();
}
