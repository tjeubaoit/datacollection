package com.datacollection.graphdb.traversal;

import com.datacollection.graphdb.Direction;
import com.datacollection.graphdb.Edge;
import com.datacollection.graphdb.GraphSession;
import com.datacollection.graphdb.Vertex;

/**
 * Biểu diễn một quá trình duyệt đồ thị
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public interface Traversal {

    /**
     * Chuyển sang bước duyệt tiếp theo
     *
     * @return object Step mô tả bước duyệt tiếp theo
     */
    Step nextStep();

    /**
     * Tạo một đối tượng Traversal biểu diễn quá trình duyệt đồ thị
     * với Direction.OUT và không có điều kiện duyệt
     *
     * @param session   GraphSession dùng để làm việc với graphdb
     * @param algorithm thuật toán duyệt đồ thị là BFS hay DFS
     * @param root      Đỉnh bắt đầu duyệt
     * @return object Traversal
     */
    static Traversal create(GraphSession session,
                            Algorithm algorithm,
                            Vertex root) {
        return create(session, algorithm, root, Direction.OUT, new AlwaysTrueCondition());
    }

    /**
     * Tạo một đối tượng Traversal biểu diễn quá trình duyệt đồ thị
     *
     * @param session   GraphSession dùng để làm việc với graphdb
     * @param algorithm thuật toán duyệt đồ thị là BFS hay DFS
     * @param root      Đỉnh bắt đầu duyệt
     * @param direction hướng duyệt
     * @param condition Quy định điều kiện duyệt đồ thị
     * @return object Traversal
     */
    static Traversal create(GraphSession session,
                            Algorithm algorithm,
                            Vertex root,
                            Direction direction,
                            Condition condition) {
        switch (algorithm) {
            case BFS:
                return new BfsTraversal(session, direction, condition, root);
            case DFS:
                return new DfsTraversal(session, direction, condition, root);
            default:
                throw new IllegalArgumentException("Invalid algorithm");
        }
    }

    /**
     * Thuật toán duyệt đồ thị là BFS hay DFS
     */
    enum Algorithm {
        BFS,
        DFS
    }

    class AlwaysTrueCondition implements Condition {

        @Override
        public boolean isValidStep(Edge edge) {
            return true;
        }
    }

    /**
     * Quy định điều kiện duyệt đồ thị
     */
    interface Condition {
        /**
         * Quy định điều kiện duyệt đồ thị, cho biết một cạnh có phải
         * là hướng duyệt hợp lệ tiếp theo hay không
         *
         * @param edge cạnh trong hướng duyệt tiếp theo cần xét
         * @return true nếu hướng duyệt tiếp theo qua cạnh này là hợp lệ
         * và false nếu không hợp lệ
         */
        boolean isValidStep(Edge edge);
    }
}
