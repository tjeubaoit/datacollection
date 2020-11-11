package com.datacollection.collect.transform;

import org.jetbrains.annotations.NonNls;

/**
 * Created by kumin on 27/11/2017.
 */
public class TransformException extends RuntimeException {
    public TransformException() {
    }

    public TransformException(@NonNls String message) {
        super(message);
    }

    public TransformException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransformException(Throwable cause) {
        super(cause);
    }
}
