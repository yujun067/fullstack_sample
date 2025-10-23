package com.moviesearch.exception;

/**
 * Exception thrown when a requested feature flag is not found
 */
public class FeatureFlagNotFoundException extends BusinessException {

    public FeatureFlagNotFoundException(String flagName) {
        super(ErrorCode.FEATURE_FLAG_NOT_FOUND, "Feature flag not found: " + flagName);
    }

    public FeatureFlagNotFoundException(String flagName, String message) {
        super(ErrorCode.FEATURE_FLAG_NOT_FOUND, message);
    }
}
