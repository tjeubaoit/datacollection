package com.datacollection.jobs.extractaddress;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class Province {

    public String name;
    public List<District> districts = new LinkedList<>();

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", this.name);
        List<Map<String, Object>> districtMap = new LinkedList<>();
        for (District d : districts) {
            districtMap.add(d.toMap());
        }
        map.put("districts", districtMap);
        return map;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
