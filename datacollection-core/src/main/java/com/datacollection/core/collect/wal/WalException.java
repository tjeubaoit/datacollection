package com.datacollection.core.collect.wal;

/**
 * Exception xảy ra trong quá trình đọc ghi và xử lý file WAL
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class WalException extends RuntimeException {

    public WalException() {
    }

    public WalException(String message) {
        super(message);
    }

    public WalException(String message, Throwable cause) {
        super(message, cause);
    }

    public WalException(Throwable cause) {
        super(cause);
    }
}
