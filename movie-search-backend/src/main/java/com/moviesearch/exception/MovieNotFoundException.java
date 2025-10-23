package com.moviesearch.exception;

/**
 * Exception thrown when a movie is not found.
 */
public class MovieNotFoundException extends BusinessException {

    public MovieNotFoundException(String message) {
        super(ErrorCode.MOVIE_NOT_FOUND, message);
    }

    public MovieNotFoundException(String message, Throwable cause) {
        super(ErrorCode.MOVIE_NOT_FOUND, message, cause);
    }
}
