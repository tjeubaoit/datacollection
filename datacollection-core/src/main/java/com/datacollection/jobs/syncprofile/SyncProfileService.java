package com.datacollection.jobs.syncprofile;


import java.io.Closeable;
import java.util.Map;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public interface SyncProfileService extends Closeable {

    Map<String, Object> findProfileByUidAsMap(String uid);

    void close();
}
