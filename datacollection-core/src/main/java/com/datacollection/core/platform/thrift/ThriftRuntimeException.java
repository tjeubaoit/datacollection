package com.datacollection.core.platform.thrift;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class ThriftRuntimeException extends RuntimeException {

    public ThriftRuntimeException() {
    }

    public ThriftRuntimeException(String message) {
        super(message);
    }

    public ThriftRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public ThriftRuntimeException(Throwable cause) {
        super(cause);
    }
}
