package com.datacollection.graphdb;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Interface cung cấp các method làm việc với các đỉnh trong Graph
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public interface VertexFunctions {

    /**
     * Lấy full thông tin của một đỉnh từ id và label
     *
     * @param id    id của đỉnh
     * @param label label của đỉnh
     * @return Object Optional chứa object Vertex tìm được (id + label + properties)
     * hoặc chứa object null nếu không có đỉnh nào với id và label như vậy
     */
    Optional<Vertex> vertex(String id, String label);

    /**
     * Find all vertices by their labels
     *
     * @param labels list label của các đỉnh cần lọc
     * @return Danh sách các đỉnh tìm được
     */
    VertexSet vertices(String... labels);

    /**
     * Tìm kiếm tất cả các đỉnh kề (có cạnh) với một đỉnh cho trước,
     * cho phép lọc theo hướng (direction) và label của cạnh giữa chúng
     *
     * @param vertex     đỉnh đang xét cần tìm các đỉnh kề
     * @param direction  hướng của cạnh giữa đỉnh đang xét và đỉnh kề của nó,
     *                   nếu không quan tâm về hướng thì để Direction.BOTH
     * @param edgeLabels list label của cạnh giữa đỉnh đang xét và đỉnh kề cần
     *                   lọc, nếu không quan tâm thì để rỗng
     * @return Danh sách các đỉnh kề thỏa mãn điều kiện
     */
    VertexSet vertices(Vertex vertex, Direction direction, String... edgeLabels);

    /**
     * Tìm kiếm tất cả các đỉnh kề (có cạnh) với một đỉnh cho trước,
     * cho phép lọc theo hướng (direction) của cạnh giữa chúng và
     * label của đỉnh kề
     *
     * @param vertex          đỉnh đang xét cần tìm các đỉnh kề
     * @param direction       hướng của cạnh giữa đỉnh đang xét và đỉnh kề của nó,
     *                        nếu không quan tâm về hướng thì để Direction.BOTH
     * @param adjVertexLabels list label của đỉnh kề với đỉnh đang xét, nếu không
     *                        quan tâm thì để rỗng
     * @return Danh sách các đỉnh kề thỏa mãn điều kiện
     */
    VertexSet verticesByAdjVertexLabels(Vertex vertex, Direction direction, String... adjVertexLabels);

    /**
     * Thêm một tập các đỉnh vào đồ thị
     *
     * @param ts       thời gian thêm đỉnh được sử dụng trong lưu trữ của graphdb,
     *                 nếu sử dụng HBase/Cassandra làm backend storage thì đây là
     *                 timestamp sử dụng khi lưu các column trong các storage này
     * @param vertices tập các đỉnh cần thêm
     * @return Future biểu diễn trạng thái của quá trình thêm đỉnh
     */
    ListenableFuture<VertexSet> addVertices(long ts, Collection<Vertex> vertices);

    /**
     * Thêm một đỉnh vào đồ thị
     *
     * @param ts     thời gian thêm đỉnh được sử dụng trong lưu trữ của graphdb,
     *               nếu sử dụng HBase/Cassandra làm backend storage thì đây là
     *               timestamp sử dụng khi lưu các column trong các storage này
     * @param vertex đỉnh cần thêm
     * @return Future biểu diễn trạng thái của quá trình thêm đỉnh
     */
    default ListenableFuture<VertexSet> addVertex(long ts, Vertex vertex) {
        return addVertices(ts, Collections.singleton(vertex));
    }

    /**
     * Thêm một đỉnh vào đồ thị
     *
     * @param id    id của đỉnh cần thêm
     * @param label label của đỉnh cần thêm
     * @param props các thuộc tính của đỉnh cần thêm
     * @return Future biểu diễn trạng thái của quá trình thêm đỉnh
     */
    default ListenableFuture<VertexSet> addVertex(String id, String label, Map<String, Object> props) {
        return addVertex(-1, Vertex.create(id, label, props));
    }

    /**
     * Thêm một đỉnh không có thuộc tính vào đồ thị
     *
     * @param id    id của đỉnh cần thêm
     * @param label label của đỉnh cần thêm
     * @return Future biểu diễn trạng thái của quá trình thêm đỉnh
     */
    default ListenableFuture<VertexSet> addVertex(String id, String label) {
        return addVertex(-1, Vertex.create(id, label));
    }

    /**
     * Thêm một đỉnh vào đồ thị
     *
     * @param vertex đỉnh cần thêm
     * @return Future biểu diễn trạng thái của quá trình thêm đỉnh
     */
    default ListenableFuture<VertexSet> addVertex(Vertex vertex) {
        return addVertex(-1, vertex);
    }

    /**
     * Thêm một tập các đỉnh vào đồ thị
     *
     * @param vertices tập các đỉnh cần thêm
     * @return Future biểu diễn trạng thái của quá trình thêm đỉnh
     */
    default ListenableFuture<VertexSet> addVertices(Collection<Vertex> vertices) {
        return addVertices(-1, vertices);
    }

    /**
     * Xóa một đỉnh khỏi đồ thị
     *
     * @param ts    thời gian xoá đỉnh được sử dụng trong lưu trữ của graphdb,
     *              nếu sử dụng HBase/Cassandra làm backend storage thì đây là
     *              timestamp sử dụng khi lưu các column trong các storage này
     * @param label label của đỉnh cần xóa
     * @param id    id của đỉnh cần xóa
     * @return Future biểu diễn trạng thái của qúa trình xóa đỉnh
     */
    ListenableFuture<VertexSet> deleteVertex(long ts, String id, String label);

    /**
     * Xóa một đỉnh khỏi đồ thị
     *
     * @param id    id của đỉnh cần xóa
     * @param label label của đỉnh cần xóa
     * @return Future biểu diễn trạng thái của qúa trình xóa đỉnh
     */
    default ListenableFuture<VertexSet> deleteVertex(String id, String label) {
        return deleteVertex(-1, id, label);
    }
}
