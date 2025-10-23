package com.featureflags.exception;

/**
 * Error code enumeration for unified management of various business exception
 * error codes.
 */
public enum ErrorCode {

    // Common error codes
    INTERNAL_SERVER_ERROR(500, "Internal server error"),
    INVALID_PARAMETER(400, "Invalid parameter"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Resource not found"),

    // Business error codes
    FLAG_NOT_FOUND(1001, "Feature flag not found"),
    FLAG_ALREADY_EXISTS(1002, "Feature flag already exists"),
    FLAG_NAME_INVALID(1003, "Feature flag name is invalid"),
    FLAG_DESCRIPTION_INVALID(1004, "Feature flag description is invalid"),
    FLAG_OPERATION_FAILED(1005, "Feature flag operation failed");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s", code, message);
    }
}
