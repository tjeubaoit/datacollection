package com.datacollection.graphdb;

import com.google.common.util.concurrent.ListenableFuture;
import com.datacollection.common.utils.IterableAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Hỗ trợ thêm tag cho các đỉnh trong đồ thị. Tag được lưu trữ
 * dưới dạng một đỉnh trong đồ thị và có cạnh tới đỉnh cần thêm tag
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class TagManager {

    private final GraphSession session;

    public TagManager(GraphSession session) {
        this.session = session;
    }

    /**
     * Thêm các tags vào đồ thị dưới dạng các đỉnh
     *
     * @param tags Các tags cần thêm
     * @return Future biểu diễn quá trình thêm tags
     */
    public ListenableFuture<VertexSet> createTags(String... tags) {
        List<Vertex> vertices = new ArrayList<>(tags.length);
        for (String tag : tags) {
            vertices.add(Vertex.create(tag, "tag"));
        }
        return session.addVertices(vertices);
    }

    /**
     * @param ts     timestamp thời gian thêm tag, dùng cho lưu trữ trong storage
     * @param vertex đỉnh cần thêm tag
     * @param tags   các tags cần thêm
     * @return Future biểu diễn quá trình thêm tags
     */
    public ListenableFuture<EdgeSet> addTags(long ts, Vertex vertex, String... tags) {
        List<Edge> edges = new ArrayList<>(tags.length);
        for (String tag : tags) {
            Vertex vTag = Vertex.create(tag, "tag");
            edges.add(Edge.create("tag", vertex, vTag));
        }
        return session.addEdges(ts, edges);
    }

    /**
     * @param vertex đỉnh cần thêm tag
     * @param tags   các tags cần thêm
     * @return Future biểu diễn quá trình thêm tags
     */
    public ListenableFuture<EdgeSet> addTags(Vertex vertex, String... tags) {
        return addTags(-1, vertex, tags);
    }

    /**
     * Tìm tất cả các đỉnh được tag bởi một tag nào đó
     *
     * @param tag tên của tag cần tìm
     * @return danh sách tất cả các đỉnh được tag bởi tag trên
     */
    public ElementSet<Vertex> findAllTaggedBy(String tag) {
        Vertex vTag = Vertex.create(tag, "tag");
        return session.vertices(vTag, Direction.BOTH, "tag");
    }

    /**
     * Lấy về tất cả các tags của một đỉnh
     *
     * @param vertex đỉnh cần lấy thông tin tag
     * @return danh sách tất cả các tag của đỉnh đang xét
     */
    public Iterable<String> getTags(Vertex vertex) {
        return IterableAdapter.from(session.vertices(vertex, Direction.OUT, "tag"),
                AbstractElement::id);
    }
}
