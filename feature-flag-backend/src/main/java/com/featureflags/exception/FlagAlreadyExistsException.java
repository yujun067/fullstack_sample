package com.featureflags.exception;

/**
 * Exception thrown when trying to create a feature flag that already exists.
 */
public class FlagAlreadyExistsException extends BusinessException {

    public FlagAlreadyExistsException(String message) {
        super(ErrorCode.FLAG_ALREADY_EXISTS, message);
    }

    public FlagAlreadyExistsException(String message, Throwable cause) {
        super(ErrorCode.FLAG_ALREADY_EXISTS, message, cause);
    }
}
