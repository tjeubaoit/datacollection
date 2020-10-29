package com.datacollection.common.tasks;

import com.datacollection.common.config.Properties;
import com.datacollection.common.utils.Threads;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Helper class giúp quản lý task dễ dàng hơn. TaskManager submit
 * các task và thực thi nó thông qua một ExecutorService. TaskManager
 * cho phép quy định trước một thresh_hold, nếu tổng số lần xảy ra lỗi
 * trong quá trình thực thi của các task được submit bởi TaskManager
 * lớn hơn thresh_hold, TaskManager sẽ reject task mới và throw Exception.
 * TaskManager không thể submit thêm task mới cho tới khi nó được reset.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class TaskManager {

    private final int errorThreshold;
    private final ExecutorService executor;
    private final AtomicInteger errorCounter = new AtomicInteger();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Initialize TaskManager from properties
     *
     * @param p        properties that will be used to read error threshold parameter
     * @param executor where tasks will be executed on
     */
    public TaskManager(Properties p, ExecutorService executor) {
        this(p.getIntProperty("task_manager.error.threshold", 100), executor);
    }

    /**
     * Initialize TaskManager with fixed error threshold
     *
     * @param errorThreshold error thresh_hold value
     */
    public TaskManager(int errorThreshold) {
        this(errorThreshold, Executors.newCachedThreadPool());
    }

    /**
     * Initialize TaskManager with fixed error threshold and an ExecutorService
     *
     * @param errorThreshold error thresh_hold value
     * @param executor       where task will be executed on
     */
    public TaskManager(int errorThreshold, ExecutorService executor) {
        this.errorThreshold = errorThreshold;
        this.executor = executor;
    }

    /**
     * Try to submit task forever until task was scheduled for execution
     *
     * @param task     task to submit
     * @param interval time sleep before try next time
     * @return Future representing the task
     */
    public Future<?> trySubmitUntilSuccess(Runnable task, long interval) {
        Future<?> future;
        while ((future = trySubmit(task)) == null) {
            Threads.sleep(interval);
        }
        return future;
    }

    /**
     * Try to submit task
     *
     * @param task task to submit
     * @return Future representing the task or null if task cannot be
     * scheduled for execution
     */
    public Future<?> trySubmit(Runnable task) {
        try {
            return submit(task);
        } catch (RejectedExecutionException e) {
            return null;
        }
    }

    /**
     * Submit task
     *
     * @param task task to submit
     * @return Future representing the task
     * @throws RejectedExecutionException if task cannot be
     *                                    scheduled for execution
     */
    public Future<?> submit(Runnable task) {
        if (errorCounter.get() > errorThreshold) {
            // wait for failed task finish
            Threads.sleep(500);
            errorCounter.decrementAndGet();

            throw new TaskErrorExceedLimitException(
                    "Number task error " + errorCounter.get() + ", threshold " + errorThreshold);
        }

        return executor.submit(() -> {
            try {
                task.run();
                if (errorCounter.get() > 0) errorCounter.decrementAndGet();
            } catch (Throwable t) {
                logger.error("Execution Exception", t);
                errorCounter.incrementAndGet();
            }
        });
    }

    /**
     * Reset to initial state
     */
    public void reset() {
        errorCounter.set(0);
    }

    /**
     * @return ExecutorService where tasks will be executed on
     */
    public ExecutorService executor() {
        return this.executor;
    }
}
