package com.datacollection.graphdb;

import com.datacollection.common.config.Properties;
import com.datacollection.common.utils.Reflects;
import com.datacollection.graphdb.backend.BackendFactory;

/**
 * Chứa các helper method để khởi tạo session làm việc với graphdb
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public interface GraphDatabase {

    /**
     * Mở một session mới để làm việc với GraphDB
     *
     * @param props các properties dùng để khởi tạo GraphSession
     * @return new GraphSession object
     */
    static GraphSession open(Properties props) {
        return open(null, props);
    }

    /**
     * Mở một session mới để làm việc với GraphDB
     *
     * @param namespace namespace của graph, dùng để phân biệt không gian
     *                  lưu trữ ở backend storage
     * @param props các properties dùng để khởi tạo GraphSession
     * @return new GraphSession object
     */
    static GraphSession open(String namespace, Properties props) {
        try {
            BackendFactory factory = Reflects.newInstance(props.getProperty("graphdb.storage.factory.class"));
            if (namespace != null) props.setProperty("graphdb.namespace", namespace);
            factory.configure(props);
            return new DefaultSession(factory);
        } catch (Throwable t) {
            throw new GraphException(t);
        }
    }
}
