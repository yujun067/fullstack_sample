package com.moviesearch.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class MaintenanceModeException extends BusinessException {

    public MaintenanceModeException(String message) {
        super(ErrorCode.MAINTENANCE_MODE, message);
    }

    public MaintenanceModeException(String message, Throwable cause) {
        super(ErrorCode.MAINTENANCE_MODE, message, cause);
    }
}
