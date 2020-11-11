package com.datacollection.matching;

public class SpamEntityException extends RuntimeException {
    public SpamEntityException() {
    }

    public SpamEntityException(String message) {
        super(message);
    }

    public SpamEntityException(String message, Throwable cause) {
        super(message, cause);
    }

    public SpamEntityException(Throwable cause) {
        super(cause);
    }
}
