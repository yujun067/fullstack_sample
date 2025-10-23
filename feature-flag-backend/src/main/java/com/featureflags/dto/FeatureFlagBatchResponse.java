package com.featureflags.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response DTO for batch feature flag requests with timestamp information.
 * Provides type safety and clear API contract.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeatureFlagBatchResponse {

    /**
     * Feature flag states with their metadata
     */
    @JsonProperty("flags")
    private Map<String, FeatureFlagInfo> flags;

    /**
     * Response timestamp for consistency checking
     */
    @JsonProperty("responseTimestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime responseTimestamp;

    /**
     * Individual feature flag information
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeatureFlagInfo {

        /**
         * Feature flag enabled status
         */
        @JsonProperty("enabled")
        private Boolean enabled;

        /**
         * Last update timestamp from database
         */
        @JsonProperty("timestamp")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
        private LocalDateTime timestamp;

        /**
         * Feature flag name
         */
        @JsonProperty("name")
        private String name;

        /**
         * Optional description
         */
        @JsonProperty("description")
        private String description;
    }
}
