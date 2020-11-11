package com.datacollection.service.notification;

import com.datacollection.common.utils.Hashings;
import com.datacollection.common.utils.Strings;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kumin on 09/11/2017.
 */
public class Message {

    public Message(String type, String... keys) {
        this.type = type;
        this.setCompositeKeys(keys);
    }

    public void setCompositeKeys(String... keys) {
        if (keys.length == 0) return;
        this.key = Hashings.sha1AsBase64(Strings.join(keys, "_"), false);
    }

    public void setKey(String key){
        this.key = key;
    }

    public String getProperty(String key) {
        return this.properties.get(key);
    }

    public void putProperty(String key, String value) {
        this.properties.put(key, value);
    }

    public void putProperties(Map<String, String> properties){
        this.properties.putAll(properties);
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public String getType() {
        return type;
    }

    public String getKey() {
        return key;
    }

    public long getVersion(){
        return this.version;
    }

    public void setVersion(long timestamp){
        this.version = timestamp;
    }

    public int getBucket() {
        return bucket;
    }

    /**
     * Only used by internal systems, you should not change this value manually
     * @param bucket bucket
     */
    public void setBucket(int bucket) {
        this.bucket = bucket;
    }

    private final Map<String, String> properties = new HashMap<>();
    private final String type;
    private String key;
    private long version;
    private int bucket = -1;

    @Override
    public String toString() {
        return Strings.format("%s %s %s %s", type, key, version, properties);
    }
}
