package com.datacollection.collect.model;

import com.datacollection.common.utils.Maps;

import java.util.Collections;
import java.util.Map;

/**
 * Represent of node in graph
 */
public class BaseEntity extends GraphElement {

    private String id;
    private String label;

    /**
     * Tạo một BaseEntity từ ID và nhãn (label)
     *
     * @param id    id of Entity
     * @param label label of Label
     */
    public BaseEntity(String id, String label) {
        this(id, label, Collections.emptyMap());
    }

    /**
     * Tạo một BaseEntity từ ID, label và các thuộc tính
     *
     * @param id        id of Entity
     * @param label     label of Label
     * @param keyValues mảng các thuộc tính của Entity, mảng phải có độ dài là một
     *                  số chẵn và tuân theo quy tắc <i>key_1</i>, <i>value_1</i>,
     *                  <i>key_2</i>, <i>value_2</i>... <i>key_n</i>, <i>value_n</i>
     */
    public BaseEntity(String id, String label, Object... keyValues) {
        this(id, label, Maps.initFromKeyValues(keyValues));
    }

    /**
     * Tạo một BaseEntity từ ID, label và các thuộc tính
     *
     * @param id         id of Entity
     * @param label      label of Label
     * @param properties Map object represent properties of Entity
     */
    public BaseEntity(String id, String label, Map<String, Object> properties) {
        super(properties);
        this.setId(id);
        this.setLabel(label);
    }

    public String id() {
        return id;
    }

    public String label() {
        return label;
    }

    public BaseEntity setId(String id) {
        this.id = id;
        return this;
    }

    public BaseEntity setLabel(String label) {
        this.label = label;
        return this;
    }

    @Override
    public String toString() {
        return label() + ":" + id();
    }
}
