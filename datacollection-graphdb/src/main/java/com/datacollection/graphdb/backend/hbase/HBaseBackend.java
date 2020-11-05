package com.datacollection.graphdb.backend.hbase;

import com.datacollection.common.config.Properties;
import com.datacollection.platform.hbase.AbstractRepository;
import org.apache.hadoop.hbase.TableName;

/**
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class HBaseBackend extends AbstractRepository {

    protected static final String DEFAULT_NAMESPACE = "default";
    private final String namespace;

    public HBaseBackend(Properties props) {
        super(props);
        this.namespace = props.getProperty("graphdb.namespace", DEFAULT_NAMESPACE);
    }

    protected final TableName getTableName(String name) {
        return DEFAULT_NAMESPACE.equals(namespace)
                ? TableName.valueOf(name) : TableName.valueOf(namespace + ":" + name);
    }
}