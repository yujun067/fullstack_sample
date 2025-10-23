package com.moviesearch.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Global exception handler for the movie search application.
 * Provides consistent error responses across all controllers.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

        /**
         * Handle external API exceptions (OMDb API failures)
         */
        @ExceptionHandler(ExternalApiException.class)
        public Mono<ResponseEntity<Map<String, Object>>> handleExternalApiException(ExternalApiException ex) {
                log.error("External API error: {}", ex.getMessage(), ex);

                Map<String, Object> errorResponse = Map.of(
                                "error", "Service Unavailable",
                                "message", ex.getMessage(),
                                "code", ex.getCode(),
                                "timestamp", LocalDateTime.now());

                return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse));
        }

        /**
         * Handle business exceptions
         */
        @ExceptionHandler(BusinessException.class)
        public Mono<ResponseEntity<Map<String, Object>>> handleBusinessException(BusinessException ex) {
                log.error("Business error: {}", ex.getMessage(), ex);

                HttpStatus status = mapErrorCodeToHttpStatus(ex.getErrorCode());

                Map<String, Object> errorResponse = Map.of(
                                "error", ex.getErrorCode().getMessage(),
                                "message", ex.getMessage(),
                                "code", ex.getCode(),
                                "timestamp", LocalDateTime.now());

                return Mono.just(ResponseEntity.status(status).body(errorResponse));
        }

        /**
         * Handle general exceptions
         */
        @ExceptionHandler(Exception.class)
        public Mono<ResponseEntity<Map<String, Object>>> handleGeneralException(Exception ex) {
                log.error("Unexpected error: {}", ex.getMessage(), ex);

                Map<String, Object> errorResponse = Map.of(
                                "error", "Internal Server Error",
                                "message", "An unexpected error occurred",
                                "code", 500,
                                "timestamp", LocalDateTime.now());

                return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
        }

        /**
         * Map error codes to appropriate HTTP status codes
         */
        private HttpStatus mapErrorCodeToHttpStatus(ErrorCode errorCode) {
                return switch (errorCode) {
                        case INVALID_PARAMETER -> HttpStatus.BAD_REQUEST;
                        case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
                        case FORBIDDEN -> HttpStatus.FORBIDDEN;
                        case NOT_FOUND, MOVIE_NOT_FOUND, FEATURE_FLAG_NOT_FOUND -> HttpStatus.NOT_FOUND;
                        case SERVICE_UNAVAILABLE, SEARCH_SERVICE_UNAVAILABLE, EXTERNAL_API_ERROR ->
                                HttpStatus.SERVICE_UNAVAILABLE;
                        case MAINTENANCE_MODE -> HttpStatus.SERVICE_UNAVAILABLE;
                        default -> HttpStatus.INTERNAL_SERVER_ERROR;
                };
        }
}