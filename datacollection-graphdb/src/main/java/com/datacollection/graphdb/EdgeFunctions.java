package com.datacollection.graphdb;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Interface cung cấp các method làm việc với các cạnh trong đồ thị
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public interface EdgeFunctions {

    /**
     * Lấy danh sách cạnh của một đỉnh, filter theo hướng và label của cạnh
     *
     * @param vertex     đỉnh đang xét
     * @param direction  hướng của cạnh cần lấy
     * @param edgeLabels list label của cạnh cần lọc, nếu không quan
     *                   tâm về label cạnh thì để rỗng
     * @return danh sách cạnh phù hợp điều kiện
     */
    EdgeSet edges(Vertex vertex, Direction direction, String... edgeLabels);

    /**
     * Lấy danh sách các cạnh của một đỉnh, filter theo hướng của cạnh và
     * danh sách label của các đỉnh đối diện.
     *
     * @param vertex          đỉnh đang xét
     * @param direction       hướng của cạnh cần lấy
     * @param adjVertexLabels list các label của đỉnh đối diện cần lọc, nếu
     *                        không quan tâm thì để rỗng
     * @return danh sách các cạnh phù hợp điều kiện
     */
    EdgeSet edgesByAdjVertexLabels(Vertex vertex, Direction direction, String... adjVertexLabels);

    /**
     * Thêm một tập các cạnh vào đồ thị
     *
     * @param ts    thời gian thêm cạnh được sử dụng trong lưu trữ của graphdb,
     *              nếu sử dụng HBase/Cassandra làm backend storage thì đây là
     *              timestamp sử dụng khi lưu các column trong các storage này
     * @param edges tập các cạnh cần thêm
     * @return Future biểu diễn trạng thái của quá trình thêm cạnh
     */
    ListenableFuture<EdgeSet> addEdges(long ts, Collection<Edge> edges);

    /**
     * Thêm một cạnh vào đồ thị
     *
     * @param ts   thời gian thêm cạnh được sử dụng trong lưu trữ của graphdb,
     *             nếu sử dụng HBase/Cassandra làm backend storage thì đây là
     *             timestamp sử dụng khi lưu các column trong các storage này
     * @param edge cạnh cần thêm
     * @return Future biểu diễn trạng thái của quá trình thêm cạnh
     */
    default ListenableFuture<EdgeSet> addEdge(long ts, Edge edge) {
        return addEdges(ts, Collections.singleton(edge));
    }

    /**
     * Thêm một cạnh vào đồ thị
     *
     * @param edge cạnh cần thêm
     * @return Future biểu diễn trạng thái của quá trình thêm cạnh
     */
    default ListenableFuture<EdgeSet> addEdge(Edge edge) {
        return addEdge(-1, edge);
    }

    /**
     * Thêm một tập các cạnh vào đồ thị
     *
     * @param edges tập các cạnh cần thêm
     * @return Future biểu diễn trạng thái của quá trình thêm cạnh
     */
    default ListenableFuture<EdgeSet> addEdges(Collection<Edge> edges) {
        return addEdges(-1, edges);
    }

    /**
     * Thêm một cạnh vào đồ thị
     *
     * @param label     label của cạnh cần thêm
     * @param outVertex đỉnh bắt đầu của cạnh cần thêm (source vertex), là
     *                  đỉnh mà từ đó cạnh có chiều đi ra
     * @param inVertex  đỉnh kết thúc của cạnh cần thêm (destination vertex),
     *                  là đỉnh mà từ đó cạnh có chiều đi vào
     * @param edgeProps danh sách các thuộc tính của cạnh cần thêm
     * @return Future biểu diễn trạng thái của quá trình thêm cạnh
     */
    default ListenableFuture<EdgeSet> addEdge(String label,
                                              Vertex outVertex,
                                              Vertex inVertex,
                                              Map<String, Object> edgeProps) {
        return addEdge(Edge.create(label, outVertex, inVertex, edgeProps));
    }

    /**
     * Thêm một cạnh vào đồ thị
     *
     * @param label     label của cạnh cần thêm
     * @param outVertex đỉnh bắt đầu của cạnh cần thêm (source vertex), là
     *                  đỉnh mà từ đó cạnh có chiều đi ra
     * @param inVertex  đỉnh kết thúc của cạnh cần thêm (destination vertex),
     *                  là đỉnh mà từ đó cạnh có chiều đi vào
     * @return Future biểu diễn trạng thái của quá trình thêm cạnh
     */
    default ListenableFuture<EdgeSet> addEdge(String label, Vertex outVertex, Vertex inVertex) {
        return addEdge(label, outVertex, inVertex, Collections.emptyMap());
    }

    /**
     * Xóa một cạnh khỏi đồ thị
     *
     * @param ts        thời gian xóa cạnh được sử dụng trong lưu trữ của graphdb,
     *                  nếu sử dụng HBase/Cassandra làm backend storage thì đây là
     *                  timestamp sử dụng khi lưu các column trong các storage này
     * @param label     label của cạnh cần xóa
     * @param outVertex đỉnh bắt đầu của cạnh cần xóa (source vertex), là
     *                  đỉnh mà từ đó cạnh có chiều đi ra
     * @param inVertex  đỉnh kết thúc của cạnh cần xóa (destination vertex),
     *                  là đỉnh mà từ đó cạnh có chiều đi vào
     * @return Future biểu diễn trạng thái của quá trình xóa cạnh
     */
    ListenableFuture<EdgeSet> removeEdge(long ts, String label, Vertex outVertex, Vertex inVertex);

    /**
     * Xóa một cạnh khỏi đồ thị
     *
     * @param label     label của cạnh cần xóa
     * @param outVertex đỉnh bắt đầu của cạnh cần xóa (source vertex), là
     *                  đỉnh mà từ đó cạnh có chiều đi ra
     * @param inVertex  đỉnh kết thúc của cạnh cần xóa (destination vertex),
     *                  là đỉnh mà từ đó cạnh có chiều đi vào
     * @return Future biểu diễn trạng thái của quá trình xóa cạnh
     */
    default ListenableFuture<EdgeSet> removeEdge(String label, Vertex outVertex, Vertex inVertex) {
        return removeEdge(-1, label, outVertex, inVertex);
    }
}
