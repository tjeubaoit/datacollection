package com.datacollection.graphdb;

import com.datacollection.graphdb.backend.EdgeBackend;
import com.datacollection.graphdb.backend.VertexBackend;

import java.io.Closeable;

/**
 * Đại diện cho một phiên làm việc với graphdb
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
    EdgeBackend edgeRepository();

    /**
     * Dựa trên Repository Pattern, chứa các hàm thao tác với dữ liệu
     * đỉnh trong graphdb
     *
     * @return VertexRepository đang được sử dụng bởi GraphSession
     */
    VertexBackend vertexRepository();

    @Override
    void close();
}
