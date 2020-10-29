package com.datacollection.common.tasks;

import java.util.concurrent.Callable;

/**
 * Đại diện cho một task, task là một đối tượng có thể chạy được
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public interface Task<V> extends Runnable, Callable<V> {

    @Override
    default void run() {
        call();
    }

    @Override
    V call();
}
