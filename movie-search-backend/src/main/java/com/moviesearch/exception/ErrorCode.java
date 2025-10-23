package com.moviesearch.exception;

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
    SERVICE_UNAVAILABLE(503, "Service unavailable"),

    // Movie search specific error codes
    MOVIE_NOT_FOUND(2001, "Movie not found"),
    SEARCH_QUERY_INVALID(2002, "Search query is invalid"),
    EXTERNAL_API_ERROR(2003, "External API error"),
    SEARCH_SERVICE_UNAVAILABLE(2004, "Search service unavailable"),
    CACHE_ERROR(2005, "Cache operation failed"),
    FEATURE_FLAG_ERROR(2006, "Feature flag operation failed"),
    FEATURE_FLAG_NOT_FOUND(2008, "Feature flag not found"),
    MAINTENANCE_MODE(2007, "Service is in maintenance mode");

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
