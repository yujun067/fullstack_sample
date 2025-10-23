package com.moviesearch.interceptor;

import com.moviesearch.exception.MaintenanceModeException;
import com.moviesearch.service.FeatureFlagConsumer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
@Slf4j
public class MaintenanceModeInterceptor implements HandlerInterceptor {

    private final FeatureFlagConsumer featureFlagConsumer;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // Check if maintenance mode is enabled
        if (featureFlagConsumer.isMaintenanceModeEnabled()) {
            log.warn("Request blocked due to maintenance mode: {} {}",
                    request.getMethod(), request.getRequestURI());
            throw new MaintenanceModeException("Service is currently under maintenance. Please try again later.");
        }
        return true;
    }
}
