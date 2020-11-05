package com.datacollection.graphdb.backend.hbase;

import com.google.common.base.Preconditions;
import com.datacollection.common.config.Properties;
import com.datacollection.graphdb.backend.EdgeBackend;
import com.datacollection.graphdb.backend.BackendFactory;
import com.datacollection.graphdb.backend.VertexBackend;

/**
 * Factory class tạo ra các Repository làm việc với HBase
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class HBackendFactory implements BackendFactory {

    private Properties props;

    @Override
    public EdgeBackend getEdgeBackend() {
        Preconditions.checkNotNull(props, "Repository Factory must be configure first");
        return new HEdgeBackend(props);
    }

    @Override
    public VertexBackend getVertexBackend() {
        Preconditions.checkNotNull(props, "Repository Factory must be configure first");
        return new HVertexBackend(props);
    }

    @Override
    public void configure(Properties p) {
        this.props = p;
    }
}
