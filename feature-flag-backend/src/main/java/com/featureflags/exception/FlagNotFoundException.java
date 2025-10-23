package com.featureflags.exception;

/**
 * Exception thrown when a feature flag is not found.
 */
public class FlagNotFoundException extends BusinessException {

    public FlagNotFoundException(String message) {
        super(ErrorCode.FLAG_NOT_FOUND, message);
    }

    public FlagNotFoundException(String message, Throwable cause) {
        super(ErrorCode.FLAG_NOT_FOUND, message, cause);
    }
}
