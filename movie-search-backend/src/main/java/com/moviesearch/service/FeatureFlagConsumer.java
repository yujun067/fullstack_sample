package com.moviesearch.service;

import com.moviesearch.config.FeatureFlagConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
        Boolean enabled = getFeatureFlag("maintenance_mode");
        return enabled != null ? enabled : getDefaultValue("maintenance_mode");
    }

    /**
     * Get a specific feature flag value directly without creating a copy of all
     * flags
     * Returns null if the flag doesn't exist
     */
    public Boolean getFeatureFlag(String flagName) {
        FeatureFlagState state = flagStates.get(flagName);
        return state != null ? state.isEnabled() : null;
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
        log.info("[CONSUMER] Received feature flag update '{}' to {} (messageId: {})",
                flagName, enabled, messageId);

        // Get current state
        FeatureFlagState currentState = flagStates.get(flagName);
        log.debug("[CONSUMER] Current state for '{}': {}", flagName,
                currentState != null ? String.format("enabled=%s, messageId=%s, lastUpdated=%s",
                        currentState.isEnabled(), currentState.getMessageId(), currentState.getLastUpdated()) : "null");

        // Check for duplicate message processing (if messageId provided)
        if (messageId != null && currentState != null && messageId.equals(currentState.getMessageId())) {
            log.debug("[CONSUMER] Ignoring duplicate message for flag '{}' with messageId: {}", flagName, messageId);
            return;
        }

        // Update the flag state atomically - accept eventual consistency
        FeatureFlagState newState = new FeatureFlagState(enabled, messageId);
        flagStates.put(flagName, newState);

        log.info("[CONSUMER] Successfully updated feature flag '{}' to {} (messageId: {})",
                flagName, enabled, messageId);
        log.debug("[CONSUMER] Current flag states: {}", flagStates.keySet());
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
