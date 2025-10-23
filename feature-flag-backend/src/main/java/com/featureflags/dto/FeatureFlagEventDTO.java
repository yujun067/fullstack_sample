package com.featureflags.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for feature flag events published to Redis pub/sub.
 * This ensures consistent message format between producer and consumer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeatureFlagEventDTO {

    /**
     * Event type: CREATED, UPDATED, DELETED
     */
    @JsonProperty("eventType")
    private EventType eventType;

    /**
     * Feature flag name
     */
    @JsonProperty("flagName")
    private String flagName;

    /**
     * Feature flag description
     */
    @JsonProperty("description")
    private String description;

    /**
     * Feature flag enabled status
     */
    @JsonProperty("enabled")
    private Boolean enabled;

    /**
     * User who triggered the event
     */
    @JsonProperty("triggeredBy")
    private String triggeredBy;

    /**
     * Event timestamp
     */
    @JsonProperty("timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime timestamp;

    /**
     * Event version for backward compatibility
     */
    @JsonProperty("version")
    @Builder.Default
    private String version = "1.0";

    /**
     * Additional metadata
     */
    @JsonProperty("metadata")
    private String metadata;

    /**
     * Message ID for deduplication and tracking
     */
    @JsonProperty("messageId")
    private String messageId;

    /**
     * Get message ID for deduplication and tracking
     */
    public String getMessageId() {
        return messageId;
    }

    public enum EventType {
        CREATED, UPDATED, DELETED
    }

    /**
     * Create event for flag creation
     */
    public static FeatureFlagEventDTO createEvent(String flagName, String description,
            Boolean enabled, String triggeredBy) {
        return createEvent(flagName, description, enabled, triggeredBy, LocalDateTime.now());
    }

    /**
     * Create event for flag creation with explicit timestamp
     */
    public static FeatureFlagEventDTO createEvent(String flagName, String description,
            Boolean enabled, String triggeredBy, LocalDateTime timestamp) {
        return createEvent(flagName, description, enabled, triggeredBy, timestamp, null);
    }

    /**
     * Create event for flag creation with explicit timestamp and message ID
     */
    public static FeatureFlagEventDTO createEvent(String flagName, String description,
            Boolean enabled, String triggeredBy, LocalDateTime timestamp, String messageId) {
        return FeatureFlagEventDTO.builder()
                .eventType(EventType.CREATED)
                .flagName(flagName)
                .description(description)
                .enabled(enabled)
                .triggeredBy(triggeredBy)
                .timestamp(timestamp)
                .messageId(messageId)
                .build();
    }

    /**
     * Create event for flag update
     */
    public static FeatureFlagEventDTO updateEvent(String flagName, String description,
            Boolean enabled, String triggeredBy) {
        return updateEvent(flagName, description, enabled, triggeredBy, LocalDateTime.now());
    }

    /**
     * Create event for flag update with explicit timestamp
     */
    public static FeatureFlagEventDTO updateEvent(String flagName, String description,
            Boolean enabled, String triggeredBy, LocalDateTime timestamp) {
        return updateEvent(flagName, description, enabled, triggeredBy, timestamp, null);
    }

    /**
     * Create event for flag update with explicit timestamp and message ID
     */
    public static FeatureFlagEventDTO updateEvent(String flagName, String description,
            Boolean enabled, String triggeredBy, LocalDateTime timestamp, String messageId) {
        return FeatureFlagEventDTO.builder()
                .eventType(EventType.UPDATED)
                .flagName(flagName)
                .description(description)
                .enabled(enabled)
                .triggeredBy(triggeredBy)
                .timestamp(timestamp)
                .messageId(messageId)
                .build();
    }

    /**
     * Create event for flag deletion
     */
    public static FeatureFlagEventDTO deleteEvent(String flagName, String triggeredBy) {
        return deleteEvent(flagName, triggeredBy, LocalDateTime.now());
    }

    /**
     * Create event for flag deletion with explicit timestamp
     */
    public static FeatureFlagEventDTO deleteEvent(String flagName, String triggeredBy, LocalDateTime timestamp) {
        return deleteEvent(flagName, triggeredBy, timestamp, null);
    }

    /**
     * Create event for flag deletion with explicit timestamp and message ID
     */
    public static FeatureFlagEventDTO deleteEvent(String flagName, String triggeredBy, LocalDateTime timestamp,
            String messageId) {
        return FeatureFlagEventDTO.builder()
                .eventType(EventType.DELETED)
                .flagName(flagName)
                .triggeredBy(triggeredBy)
                .timestamp(timestamp)
                .messageId(messageId)
                .build();
    }
}
