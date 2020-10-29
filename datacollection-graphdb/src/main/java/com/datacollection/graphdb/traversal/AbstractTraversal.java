package com.datacollection.graphdb.traversal;

import com.google.common.base.Preconditions;
import com.datacollection.graphdb.Direction;
import com.datacollection.graphdb.GraphSession;
import com.datacollection.graphdb.Vertex;

/**
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public abstract class AbstractTraversal implements Traversal {

    private final GraphSession session;
    private final Direction direction;
    private final Condition condition;
    private final Vertex root;

    public AbstractTraversal(GraphSession session, Direction direction,
                             Condition condition, Vertex root) {
        Preconditions.checkNotNull(root, "Root vertex cannot be null");
        Preconditions.checkNotNull(session);

        this.session = session;
        this.direction = direction;
        this.condition = condition;
        this.root = root;
    }

    /**
     * @return GraphSession dùng để làm việc với graphdb
     */
    public GraphSession getSession() {
        return session;
    }

    /**
     * @return hướng duyệt hiện tại
     */
    public Direction getDirection() {
        return direction;
    }

    /**
     * @return Condition quy định điều kiện duyệt
     */
    public Condition getCondition() {
        return condition;
    }

    /**
     * @return Đỉnh bắt đầu duyệt
     */
    public Vertex getRoot() {
        return root;
    }
}
