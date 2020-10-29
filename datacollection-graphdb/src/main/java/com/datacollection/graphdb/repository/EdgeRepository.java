package com.datacollection.graphdb.repository;

import com.datacollection.graphdb.Direction;
import com.datacollection.graphdb.Edge;
import com.datacollection.graphdb.Vertex;

/**
 * Dựa trên Repository Pattern, chứa các hàm thao tác với dữ liệu
 * cạnh trong graphdb
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public interface EdgeRepository extends CrudRepository<Edge> {

    /**
     * Tìm tất cả các cạnh của một đỉnh, lọc theo hướng, label cạnh
     *
     * @param src       đỉnh đang xét
     * @param direction hướng của cạnh
     * @param label     label của cạnh
     * @return Danh sách các cạnh phù hợp
     */
    Iterable<Edge> findByVertex(Vertex src, Direction direction, String label);
}
