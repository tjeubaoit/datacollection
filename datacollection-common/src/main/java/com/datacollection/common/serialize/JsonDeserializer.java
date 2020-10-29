package com.datacollection.common.serialize;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class JsonDeserializer<T> implements Deserializer<T> {

    private final ObjectMapper om = new ObjectMapper();
    private final Class<T> clazz;

    public JsonDeserializer(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public T deserialize(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return om.readValue(sb.toString(), clazz);
        }
    }

    @Override
    public T deserialize(byte[] serialized) throws IOException {
        return deserialize(new ByteArrayInputStream(serialized));
    }
}
