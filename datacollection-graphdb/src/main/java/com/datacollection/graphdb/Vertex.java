package com.datacollection.graphdb;

import java.util.Collections;
import java.util.Map;

/**
 * Đại diện cho một đỉnh trong đồ thị
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class Vertex extends AbstractElement {

    private Vertex(String id, String label, Map<String, ?> props) {
        super(id, label, props);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Vertex) {
            Vertex v = ((Vertex) obj);
            return v.id().equals(this.id()) && v.label().equals(this.label());
        }
        return false;
    }

    /**
     * Khởi tạo một đối tượng Vertex chứa thông tin về một đỉnh.
     * Lưu ý đây chỉ là một object thông thường và đỉnh có thông tin
     * chứa trong object này chưa tồn tại trong đồ thị
     *
     * @param id    id của đỉnh cần tạo
     * @param label label của đỉnh cần tạo
     * @param props các thuộc tính của đỉnh cần tạo
     * @return object Vertex chứa thông tin của đỉnh cần tạo
     */
    public static Vertex create(String id, String label, Map<String, ?> props) {
        return new Vertex(id, label, props);
    }

    /**
     * Khởi tạo một đối tượng Vertex chứa thông tin về một đỉnh.
     * Lưu ý đây chỉ là một object thông thường và đỉnh có thông tin
     * chứa trong object này chưa tồn tại trong đồ thị
     *
     * @param id    id của đỉnh cần tạo
     * @param label label của đỉnh cần tạo
     * @return object Vertex chứa thông tin của đỉnh cần tạo
     */
    public static Vertex create(String id, String label) {
        return new Vertex(id, label, Collections.emptyMap());
    }
}
