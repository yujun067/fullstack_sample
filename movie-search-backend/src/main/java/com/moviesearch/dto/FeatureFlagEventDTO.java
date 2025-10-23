package com.moviesearch.dto;

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
 * 
 * Note: This is a copy of the DTO from feature-flag-backend to maintain
 * consistency across services. In a microservices architecture, this could
 * be shared via a common library or API.
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

    public enum EventType {
        CREATED, UPDATED, DELETED
    }
}
