package com.datacollection.matching;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kumin on 27/10/2017.
 */
public class ProfileLog {
    public String uid;

    public String type;

//    public Collection<String> entities = new HashSet<>();

    public Map<String, Integer> entityLogs = new HashMap<>();
}
