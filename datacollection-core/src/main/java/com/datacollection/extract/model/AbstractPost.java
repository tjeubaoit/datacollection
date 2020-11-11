package com.datacollection.extract.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.avro.reflect.Nullable;

import java.io.IOException;
import java.util.Map;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public abstract class AbstractPost {

    private static final ObjectMapper om;
    static {
        om = new ObjectMapper();
        om.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
    }

    public static AbstractPost empty() {
        return new AbstractPost() {};
    }

    @Nullable
    public String content;

    public String toJson() {
        try {
            return om.writeValueAsString(this);
        } catch (IOException e) {
            return toString();
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> toMap() {
        return om.convertValue(this, Map.class);
    }
}
