package com.datacollection.graphdb.backend.cassandra;

import com.datacollection.common.config.Properties;
import com.datacollection.platform.cassandra.AbstractRepository;

import java.io.Closeable;

/**
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public abstract class CassandraBackend extends AbstractRepository implements Closeable {

    public CassandraBackend(Properties props) {
        super(props);
    }
}
