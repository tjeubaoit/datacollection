package com.datacollection.core.extract.raw.xlsx.model;

import java.util.List;
//@JsonIgnoreProperties({"key"})
public class KeyValueModel {
    private String key;
    private List<String> value;

    public KeyValueModel() {
    }

    public KeyValueModel(String key, List<String> value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<String> getValue() {
        return value;
    }

    public void setValue(List<String> value) {
        this.value = value;
    }
}
