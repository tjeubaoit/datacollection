package com.datacollection.graphdb.backend;

import com.datacollection.graphdb.Direction;
import com.datacollection.graphdb.Edge;
import com.datacollection.graphdb.Vertex;

public interface EdgeBackend extends GraphBackend<Edge> {

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
