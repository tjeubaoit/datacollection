package com.datacollection.graphdb.hbase;

import com.google.common.base.Preconditions;
import com.datacollection.common.config.Properties;
import com.datacollection.graphdb.repository.EdgeRepository;
import com.datacollection.graphdb.repository.RepositoryFactory;
import com.datacollection.graphdb.repository.VertexRepository;

/**
 * Factory class tạo ra các Repository làm việc với HBase
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class HRepositoryFactory implements RepositoryFactory {

    private Properties props;

    @Override
    public EdgeRepository edgeRepository() {
        Preconditions.checkNotNull(props, "Repository Factory must be configure first");
        return new HEdgeRepository(props);
    }

    @Override
    public VertexRepository vertexRepository() {
        Preconditions.checkNotNull(props, "Repository Factory must be configure first");
        return new HVertexRepository(props);
    }

    @Override
    public void configure(Properties p) {
        this.props = p;
    }
}
