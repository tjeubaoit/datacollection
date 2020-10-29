package com.datacollection.graphdb;

/**
 * Enum định nghĩa các hướng của một cạnh trong đồ thị
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public enum Direction {

    OUT,
    IN,
    BOTH;

    public static final Direction[] proper = new Direction[]{OUT, IN};

    private Direction() {
    }

    public Direction opposite() {
        if (this.equals(OUT)) {
            return IN;
        } else {
            return this.equals(IN) ? OUT : BOTH;
        }
    }
}
