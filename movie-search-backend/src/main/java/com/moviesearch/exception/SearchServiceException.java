package com.moviesearch.exception;

/**
 * Exception thrown when search service operations fail.
 */
public class SearchServiceException extends BusinessException {

    public SearchServiceException(String message) {
        super(ErrorCode.SEARCH_SERVICE_UNAVAILABLE, message);
    }

    public SearchServiceException(String message, Throwable cause) {
        super(ErrorCode.SEARCH_SERVICE_UNAVAILABLE, message, cause);
    }
}
