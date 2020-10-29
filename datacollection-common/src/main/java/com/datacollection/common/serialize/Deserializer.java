package com.datacollection.common.serialize;

import java.io.IOException;
import java.io.InputStream;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public interface Deserializer<T> {

    T deserialize(InputStream in) throws IOException;

    T deserialize(byte[] serialized) throws IOException;
}
