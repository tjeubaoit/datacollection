package com.datacollection.graphdb;

import com.datacollection.graphdb.repository.EdgeRepository;
import com.datacollection.graphdb.repository.VertexRepository;

import java.io.Closeable;

/**
 * Đại diện cho một session khi làm việc với graphdb
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public interface GraphSession extends VertexFunctions, EdgeFunctions, Closeable {

    /**
     * Dựa trên Repository Pattern, chứa các hàm thao tác với dữ liệu
     * cạnh trong graphdb
     *
     * @return EdgeRespository đang được sử dụng bởi GraphSession
     */
    EdgeRepository edgeRepository();

    /**
     * Dựa trên Repository Pattern, chứa các hàm thao tác với dữ liệu
     * đỉnh trong graphdb
     *
     * @return VertexRepository đang được sử dụng bởi GraphSession
     */
    VertexRepository vertexRepository();

    @Override
    void close();
}
