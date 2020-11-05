package com.datacollection.graphdb.backend;

import com.datacollection.graphdb.Vertex;

public interface VertexBackend extends GraphBackend<Vertex> {

    /**
     * Lấy thông tin một đỉnh trong graphdb
     *
     * @param label label đỉnh cần tìm
     * @param id    ID đỉnh cần tìm
     * @return object Vertex chứa toàn bộ thông tin đỉnh cần tìm
     */
    Vertex findOne(String label, String id);

    /**
     * Tìm tất cả các đỉnh theo một label
     *
     * @param label label của các đỉnh cần tìm
     * @return Danh sách tất cả các đỉnh cần tìm
     */
    Iterable<Vertex> findByLabel(String label);
}
