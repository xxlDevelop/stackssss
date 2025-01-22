package org.yx.hoststack.center.common.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * Custom exception class for handling image-related errors.
 */
@Getter
@Setter
public class ImageException extends RuntimeException {

    /**
     * -- GETTER --
     *  Gets the error code.
     */
    private final int code;

    /**
     * Constructor with code and message.
     *
     * @param code    the error code
     * @param message the error message
     */
    public ImageException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * Constructor with code, message, and cause.
     *
     * @param code    the error code
     * @param message the error message
     * @param cause   the root cause of the exception
     */
    public ImageException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

}
