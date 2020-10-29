package com.datacollection.common.config;

/**
 * Exception xảy ra khi có lỗi trong quá trình load các config
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class ConfigurationException extends RuntimeException {

    public ConfigurationException() {
    }

    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigurationException(Throwable cause) {
        super(cause);
    }

    public ConfigurationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
