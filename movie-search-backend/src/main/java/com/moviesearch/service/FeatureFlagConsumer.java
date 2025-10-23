package com.moviesearch.service;

import com.moviesearch.config.FeatureFlagConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeatureFlagConsumer {

    // In-memory cache for subscribed feature flags with metadata
    private final Map<String, FeatureFlagState> flagStates = new ConcurrentHashMap<>();

    // Configuration for feature flag defaults
    private final FeatureFlagConfig featureFlagConfig;

    /**
     * Internal state for feature flags with message deduplication support.
     * Simplified to focus on essential functionality.
     */
    public static class FeatureFlagState {
        private final boolean enabled;
        private final String messageId;
        private final LocalDateTime lastUpdated;

        public FeatureFlagState(boolean enabled, String messageId) {
            this.enabled = enabled;
            this.messageId = messageId;
            this.lastUpdated = LocalDateTime.now();
        }

        public boolean isEnabled() {
            return enabled;
        }

        public String getMessageId() {
            return messageId;
        }

        public LocalDateTime getLastUpdated() {
            return lastUpdated;
        }
    }

    /**
     * Check if maintenance mode is enabled with fallback mechanism
     */
    public boolean isMaintenanceModeEnabled() {
        try {
            FeatureFlagState state = flagStates.get("maintenance_mode");
            return state != null ? state.isEnabled() : getDefaultValue("maintenance_mode");
        } catch (Exception e) {
            log.warn("⚠️ [FALLBACK] Feature flag check failed for maintenance_mode, using default: {}", e.getMessage());
            return getDefaultValue("maintenance_mode");
        }
    }

    /**
     * Check if dark mode is enabled with fallback mechanism
     */
    public boolean isDarkModeEnabled() {
        try {
            FeatureFlagState state = flagStates.get("dark_mode");
            return state != null ? state.isEnabled() : getDefaultValue("dark_mode");
        } catch (Exception e) {
            log.warn("⚠️ [FALLBACK] Feature flag check failed for dark_mode, using default: {}", e.getMessage());
            return getDefaultValue("dark_mode");
        }
    }

    /**
     * Get all feature flags with fallback mechanism
     * Returns a defensive copy to prevent external modification
     */
    public Map<String, Boolean> getAllFeatureFlags() {
        try {
            Map<String, Boolean> result = new ConcurrentHashMap<>();
            flagStates.forEach((name, state) -> result.put(name, state.isEnabled()));
            return result;
        } catch (Exception e) {
            log.warn("⚠️ [FALLBACK] Failed to get all feature flags, returning empty map: {}", e.getMessage());
            return new ConcurrentHashMap<>();
        }
    }

    /**
     * Check if a specific feature flag is enabled with fallback mechanism
     */
    public boolean isFeatureFlagEnabled(String flagName, boolean defaultValue) {
        try {
            FeatureFlagState state = flagStates.get(flagName);
            return state != null ? state.isEnabled() : defaultValue;
        } catch (Exception e) {
            log.warn("⚠️ [FALLBACK] Feature flag check failed for '{}', using default value {}: {}",
                    flagName, defaultValue, e.getMessage());
            return defaultValue;
        }
    }

    /**
     * Get feature flags with metadata for monitoring and debugging
     * Returns both flag states and their metadata
     */
    public Map<String, FeatureFlagState> getAllFeatureFlagsWithMetadata() {
        return Map.copyOf(flagStates);
    }

    /**
     * Get multiple feature flags by names with metadata
     * This method provides atomic access to prevent race conditions
     */
    public Map<String, FeatureFlagState> getFeatureFlagsWithMetadata(List<String> flagNames) {
        Map<String, FeatureFlagState> result = new ConcurrentHashMap<>();
        for (String flagName : flagNames) {
            FeatureFlagState state = flagStates.get(flagName);
            if (state != null) {
                result.put(flagName, state);
            }
        }
        return result;
    }

    /**
     * Update feature flag status (for periodic refresh without messageId)
     */
    public void updateFeatureFlag(String flagName, boolean enabled) {
        log.debug("Updating feature flag '{}' to {} (periodic refresh)", flagName, enabled);
        updateFeatureFlag(flagName, enabled, null);
    }

    /**
     * Update feature flag status with message ID for deduplication
     * This method prevents duplicate message processing and accepts eventual
     * consistency
     */
    public void updateFeatureFlag(String flagName, boolean enabled, String messageId) {
        log.info("🔄 [CONSUMER] Received feature flag update '{}' to {} (messageId: {})",
                flagName, enabled, messageId);

        // Get current state
        FeatureFlagState currentState = flagStates.get(flagName);
        log.debug("🔄 [CONSUMER] Current state for '{}': {}", flagName,
                currentState != null ? String.format("enabled=%s, messageId=%s, lastUpdated=%s",
                        currentState.isEnabled(), currentState.getMessageId(), currentState.getLastUpdated()) : "null");

        // Check for duplicate message processing (if messageId provided)
        if (messageId != null && currentState != null && messageId.equals(currentState.getMessageId())) {
            log.debug("🔄 [CONSUMER] Ignoring duplicate message for flag '{}' with messageId: {}", flagName, messageId);
            return;
        }

        // Update the flag state atomically - accept eventual consistency
        FeatureFlagState newState = new FeatureFlagState(enabled, messageId);
        flagStates.put(flagName, newState);

        log.info("✅ [CONSUMER] Successfully updated feature flag '{}' to {} (messageId: {})",
                flagName, enabled, messageId);
        log.debug("✅ [CONSUMER] Current flag states: {}", flagStates.keySet());
    }

    /**
     * Remove feature flag
     */
    public void removeFeatureFlag(String flagName) {
        log.info("Removing feature flag '{}'", flagName);
        flagStates.remove(flagName);
    }

    /**
     * Initialize feature flags with default values from configuration
     */
    public void initializeFeatureFlags() {
        log.info("Initializing feature flags with default values from configuration");
        if (featureFlagConfig.getSubscribed() != null) {
            featureFlagConfig.getSubscribed().forEach((flagName, defaultValue) -> {
                log.debug("Initializing feature flag '{}' with default value: {}", flagName, defaultValue);
                updateFeatureFlag(flagName, defaultValue);
            });
        }
    }

    /**
     * Get default value for a feature flag from configuration
     * Falls back to false if not configured
     */
    private boolean getDefaultValue(String flagName) {
        if (featureFlagConfig.getSubscribed() != null && featureFlagConfig.getSubscribed().containsKey(flagName)) {
            return featureFlagConfig.getSubscribed().get(flagName);
        }

        // Fallback to false if not configured
        log.error("No default value configured for feature flag '{}', using false as fallback", flagName);
        return false;
    }

}
