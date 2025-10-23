package com.featureflags.service;

import com.featureflags.dto.FeatureFlagEventDTO;
import com.featureflags.entity.FeatureFlag;
import com.featureflags.util.MessageIdGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Service for publishing structured feature flag events to Redis pub/sub.
 * Uses standardized event format for better reliability and maintainability.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MessagePublisherService {

    // Unified channel for all feature flag events
    private static final String FEATURE_FLAG_EVENTS_CHANNEL = "feature-flag-events";

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Publish a feature flag creation event.
     * This method will not throw exceptions to avoid affecting the main business
     * flow.
     */
    public void publishFlagCreated(FeatureFlag flag) {
        try {
            String messageId = MessageIdGenerator.generateMessageId(flag.getName());
            FeatureFlagEventDTO event = FeatureFlagEventDTO.createEvent(
                    flag.getName(),
                    flag.getDescription(),
                    flag.getEnabled(),
                    flag.getCreatedBy(),
                    flag.getCreatedAt(),
                    messageId);
            publishEventSafely(event);
            log.info("Published flag creation event for flag: {} at {} with messageId: {}",
                    flag.getName(), flag.getCreatedAt(), messageId);
        } catch (Exception e) {
            log.error(
                    "Failed to publish flag creation event for flag: {} - This will not affect the main business operation",
                    flag.getName(), e);
            // Redis failure is logged but doesn't affect the main business flow
            // Consumer will get the latest state via periodic polling
        }
    }

    /**
     * Publish a feature flag update event.
     * This method will not throw exceptions to avoid affecting the main business
     * flow.
     */
    public void publishFlagUpdated(FeatureFlag flag) {
        try {
            String messageId = MessageIdGenerator.generateMessageId(flag.getName());
            FeatureFlagEventDTO event = FeatureFlagEventDTO.updateEvent(
                    flag.getName(),
                    flag.getDescription(),
                    flag.getEnabled(),
                    flag.getUpdatedBy(),
                    flag.getUpdatedAt(),
                    messageId);
            publishEventSafely(event);
            log.info("Published flag update event for flag: {} at {} with messageId: {}",
                    flag.getName(), flag.getUpdatedAt(), messageId);
        } catch (Exception e) {
            log.error(
                    "Failed to publish flag update event for flag: {} - This will not affect the main business operation",
                    flag.getName(), e);
            // Redis failure is logged but doesn't affect the main business flow
            // Consumer will get the latest state via periodic polling
        }
    }

    /**
     * Publish a feature flag deletion event.
     * This method will not throw exceptions to avoid affecting the main business
     * flow.
     */
    public void publishFlagDeleted(FeatureFlag flag) {
        try {
            String messageId = MessageIdGenerator.generateMessageId(flag.getName());
            FeatureFlagEventDTO event = FeatureFlagEventDTO.deleteEvent(
                    flag.getName(),
                    flag.getUpdatedBy(),
                    flag.getUpdatedAt(),
                    messageId);
            publishEventSafely(event);
            log.info("Published flag deletion event for flag: {} at {} with messageId: {}",
                    flag.getName(), flag.getUpdatedAt(), messageId);
        } catch (Exception e) {
            log.error(
                    "Failed to publish flag deletion event for flag: {} - This will not affect the main business operation",
                    flag.getName(), e);
            // Redis failure is logged but doesn't affect the main business flow
            // Consumer will get the latest state via periodic polling
        }
    }

    /**
     * Safe method to publish events without throwing exceptions.
     * This method will log errors but not propagate them to avoid affecting
     * business operations.
     */
    private void publishEventSafely(FeatureFlagEventDTO event) {
        try {
            log.debug("🚀 [PUB/SUB] Starting to publish feature flag event: {}", event.getFlagName());
            log.debug("🚀 [PUB/SUB] Event details - Type: {}, Enabled: {}, Timestamp: {}, MessageId: {}",
                    event.getEventType(), event.getEnabled(), event.getTimestamp(), event.getMessageId());

            String message = objectMapper.writeValueAsString(event);
            log.info("🚀 [PUB/SUB] Serialized message: {}", message);

            redisTemplate.convertAndSend(FEATURE_FLAG_EVENTS_CHANNEL, message);
            log.debug("✅ [PUB/SUB] Successfully published event to channel '{}' for flag '{}'",
                    FEATURE_FLAG_EVENTS_CHANNEL, event.getFlagName());
        } catch (Exception e) {
            log.error(
                    "❌ [PUB/SUB] Failed to publish event to channel '{}' for flag '{}': {} - This will not affect the main business operation",
                    FEATURE_FLAG_EVENTS_CHANNEL, event.getFlagName(), e.getMessage(), e);
            // Don't rethrow - let the calling method handle the failure gracefully
        }
    }

}
