package com.datacollection.common.serialize;

import java.io.IOException;
import java.io.OutputStream;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public interface Serializer<T> {

    void serialize(T input, OutputStream out) throws IOException;

    byte[] serialize(T input) throws IOException;

    default String serializeAsString(T raw) throws IOException {
        throw new UnsupportedOperationException();
    }
}
