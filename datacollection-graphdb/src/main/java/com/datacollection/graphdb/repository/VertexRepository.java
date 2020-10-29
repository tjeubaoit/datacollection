package com.datacollection.graphdb.repository;

import com.datacollection.graphdb.Vertex;

/**
 * Dựa trên Repository Pattern, chứa các hàm thao tác với dữ liệu
 * đỉnh trong graphdb
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public interface VertexRepository extends CrudRepository<Vertex> {

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
