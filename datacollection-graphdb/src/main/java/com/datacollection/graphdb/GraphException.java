package com.datacollection.graphdb;

/**
 * Exception xảy ra khi thực hiện các thao tác trong đồ thị
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class GraphException extends RuntimeException {

    public GraphException() {
    }

    public GraphException(String message) {
        super(message);
    }

    public GraphException(String message, Throwable cause) {
        super(message, cause);
    }

    public GraphException(Throwable cause) {
        super(cause);
    }
}
