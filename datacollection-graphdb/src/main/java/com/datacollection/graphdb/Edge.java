package com.datacollection.graphdb;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

/**
 * Đại diện cho một cạnh trong đồ thị
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class Edge extends AbstractElement {

    private final Vertex inVertex;
    private final Vertex outVertex;

    private Edge(String label, Vertex inVertex, Vertex outVertex, Map<String, ?> properties) {
        super(null, label, properties);
        this.inVertex = inVertex;
        this.outVertex = outVertex;
    }

    /**
     * Lấy về một hoặc hai đỉnh của cạnh dựa theo hướng theo nguyên tắc:
     * <ul>
     * <li>Nếu Direction.BOTH thì trả về cả hai đỉnh</li>
     * <li>Nếu Direction.OUT thì trả về đỉnh mà cạnh đi ra (destination vertex)</li>
     * <li>Nếu Direction.IN thì trả về đỉnh mà cạnh đi vào (source vertex)</li>
     * </ul>
     * <p>
     * Lưu ý là các đỉnh trả về ở đây chỉ bao gồm thông tin về id và label, không
     * có thông tin về các thuộc tính. Muốn có thông tin thuộc tính cần gọi hàm
     * lấy full thông tin của đỉnh. Tham khảo VertexFunctions
     *
     * @param direction hướng dùng để filter đỉnh cần lấy
     * @return Một hoặc hai đỉnh của cạnh theo nguyên tắc phía trên. Lưu y
     */
    public Iterator<Vertex> vertices(Direction direction) {
        return direction == Direction.BOTH
                ? Arrays.asList(getVertex(Direction.OUT), getVertex(Direction.IN)).iterator()
                : Collections.singleton(getVertex(direction)).iterator();
    }

    /**
     * Lấy thông tin của đỉnh mà từ đó cạnh đi ra.
     * <p>
     * Lưu ý là các đỉnh trả về ở đây chỉ bao gồm thông tin về id và label, không
     * có thông tin về các thuộc tính. Muốn có thông tin thuộc tính cần gọi hàm
     * lấy full thông tin của đỉnh. Tham khảo VertexFunctions
     *
     * @return object Vertext chứa thông tin id + label của destination vertex
     */
    public Vertex outVertex() {
        return this.vertices(Direction.OUT).next();
    }

    /**
     * Lấy thông tin của đỉnh mà từ đó cạnh đi vào.
     * <p>
     * Lưu ý là các đỉnh trả về ở đây chỉ bao gồm thông tin về id và label, không
     * có thông tin về các thuộc tính. Muốn có thông tin thuộc tính cần gọi hàm
     * lấy full thông tin của đỉnh. Tham khảo VertexFunctions
     *
     * @return object Vertext chứa thông tin id + label của source vertex
     */
    public Vertex inVertex() {
        return this.vertices(Direction.IN).next();
    }

    private Vertex getVertex(Direction direction) throws IllegalArgumentException {
        if (!Direction.IN.equals(direction) && !Direction.OUT.equals(direction)) {
            throw new IllegalArgumentException("Invalid direction: " + direction);
        }
        return Direction.IN.equals(direction) ? inVertex : outVertex;
    }

    @Override
    public String toString() {
        return outVertex + "--" + label() + "-->" + inVertex;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Edge) {
            Edge e = ((Edge) obj);
            return e.inVertex.equals(this.inVertex)
                    && e.outVertex.equals(this.outVertex)
                    && e.label().equals(this.label());
        }
        return false;
    }

    /**
     * Khởi tạo một object Edge chứa thông tin về một cạnh. Lưu ý đây chỉ là
     * một object thông thường và cạnh có thông tin chứa trong object này chưa
     * tồn tại trong đồ thị
     *
     * @param label     label của cạnh cần tạo
     * @param outVertex đỉnh bắt đầu nơi cạnh có hướng đi ra
     * @param inVertex  đỉnh kết thúc nơi cạnh có hướng đi vào
     * @return object Edge chứa thông tin của cạnh cần tạo
     */
    public static Edge create(String label, Vertex outVertex, Vertex inVertex) {
        return new Edge(label, inVertex, outVertex, Collections.emptyMap());
    }

    /**
     * Khởi tạo một object Edge chứa thông tin về một cạnh. Lưu ý đây chỉ là
     * một object thông thường và cạnh có thông tin chứa trong object này chưa
     * tồn tại trong đồ thị
     *
     * @param label      label của cạnh cần tạo
     * @param outVertex  đỉnh bắt đầu nơi cạnh có hướng đi ra
     * @param inVertex   đỉnh kết thúc nơi cạnh có hướng đi vào
     * @param properties các thuộc tính của cạnh cần tạo
     * @return object Edge chứa thông tin của cạnh cần tạo
     */
    public static Edge create(String label, Vertex outVertex, Vertex inVertex, Map<String, ?> properties) {
        return new Edge(label, inVertex, outVertex, properties);
    }
}
