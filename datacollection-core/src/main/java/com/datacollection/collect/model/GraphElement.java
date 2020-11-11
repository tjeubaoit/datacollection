package com.datacollection.collect.model;

import com.datacollection.common.utils.Maps;

import java.util.Map;
import java.util.TreeMap;

/**
 * Abstract model for all elements in graph model (entity and relationship)
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public abstract class GraphElement {

    private final Map<String, Object> properties = new TreeMap<>();

    /**
     * Create new GraphElement object
     *
     * @param properties Map object represent properties of Entity
     */
    public GraphElement(Map<String, Object> properties) {
        properties.forEach(this::putProperty);
    }

    /**
     * @return properties of GraphElement as Map
     */
    public Map<String, Object> properties() {
        return properties;
    }

    /**
     * Get a property value from key
     *
     * @param key key of property
     * @return value of property
     */
    public Object property(String key) {
        return this.properties.get(key);
    }

    /**
     * Put or update a property
     *
     * @param key   key of property
     * @param value value of property
     */
    public void putProperty(String key, Object value) {
        // Ignore null or empty properties
        Maps.putIfNotNullOrEmpty(properties, key, value);
    }

    /**
     * Put or update multi properties
     *
     * @param map Map object represent properties
     */
    public void putProperties(Map<String, Object> map) {
        map.forEach(this::putProperty);
    }
}
