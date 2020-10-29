package com.datacollection.core.jobs.extractaddress;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class District {

    public String name;
    public List<String> communes = new LinkedList<>();

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", this.name);
        map.put("communes", this.communes);
        return map;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
