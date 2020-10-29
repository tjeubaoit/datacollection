package com.datacollection.common.tasks;

/**
 * Exception xảy ra khi số lần execute task xảy ra lỗi vượt quá một
 * giới hạn nào đó trong TaskManager
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class TaskErrorExceedLimitException extends RuntimeException {

    public TaskErrorExceedLimitException() {
    }

    public TaskErrorExceedLimitException(String message) {
        super(message);
    }

    public TaskErrorExceedLimitException(String message, Throwable cause) {
        super(message, cause);
    }

    public TaskErrorExceedLimitException(Throwable cause) {
        super(cause);
    }
}
