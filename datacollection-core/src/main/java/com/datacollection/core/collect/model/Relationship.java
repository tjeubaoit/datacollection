package com.datacollection.core.collect.model;

import com.datacollection.common.utils.Maps;

import java.util.Collections;
import java.util.Map;

/**
 * Represent of edge in graph
 */
public class Relationship extends GraphElement {

    private final String name;

    /**
     * Tạo một Relationship từ tên
     *
     * @param name name of relationship
     */
    public Relationship(String name) {
        this(name, Collections.emptyMap());
    }

    /**
     * Tạo một Relationship từ tên và các thuộc tính
     *
     * @param name      name of relationship
     * @param keyValues mảng các thuộc tính của Relationship, mảng phải có độ dài
     *                  là một số chẵn và tuân theo quy tắc <i>key_1</i>, <i>value_1</i>,
     *                  <i>key_2</i>, <i>value_2</i>... <i>key_n</i>, <i>value_n</i>
     */
    public Relationship(String name, Object... keyValues) {
        this(name, Maps.initFromKeyValues(keyValues));
    }

    /**
     * Tạo một Relationship từ tên và các thuộc tính
     *
     * @param name       name of relationship
     * @param properties Map object represent properties of Relationship
     */
    public Relationship(String name, Map<String, Object> properties) {
        super(properties);
        this.name = name;
    }

    /**
     * @return name of relationship
     */
    public String name() {
        return name;
    }

    /**
     * Create simple relationship from name
     *
     * @param name name of Relationship object to be created
     * @return new Relationship object
     */
    public static Relationship forName(String name) {
        return new Relationship(name);
    }
}
