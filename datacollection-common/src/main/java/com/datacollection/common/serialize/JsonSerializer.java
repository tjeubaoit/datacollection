package com.datacollection.common.serialize;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class JsonSerializer<T> implements Serializer<T> {

    private final ObjectMapper om = new ObjectMapper();

    public JsonSerializer(Class<T> clazz) {
        om.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
    }

    @Override
    public void serialize(T input, OutputStream out) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out))) {
            writer.write(om.writeValueAsString(input));
        }
    }

    @Override
    public byte[] serialize(T input) throws IOException {
        return om.writeValueAsBytes(input);
    }

    @Override
    public String serializeAsString(T raw) throws IOException {
        return om.writeValueAsString(raw);
    }
}
