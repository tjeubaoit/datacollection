package com.datacollection.graphdb.backend.cassandra;

import com.datacollection.graphdb.backend.BackendFactory;
import com.google.common.base.Preconditions;
import com.datacollection.common.config.Properties;
import com.datacollection.graphdb.backend.EdgeBackend;
import com.datacollection.graphdb.backend.VertexBackend;

/**
 * Factory class tạo ra các backend làm việc với Cassandra
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class CBackendFactory implements BackendFactory {

    private Properties props;

    @Override
    public EdgeBackend getEdgeBackend() {
        Preconditions.checkNotNull(props, "Backend Factory must be configure first");
        return new CEdgeBackend(props);
    }

    @Override
    public VertexBackend getVertexBackend() {
        Preconditions.checkNotNull(props, "Backend Factory must be configure first");
        return new CVertexBackend(props);
    }

    @Override
    public void configure(Properties p) {
        this.props = p;
    }
}
