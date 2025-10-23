package com.featureflags.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Feature Flag entity representing a boolean feature flag in the system.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeatureFlag {

    private Long id;
    private String name;
    private String description;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    // Constructor with required fields (excluding id and timestamps)
    public FeatureFlag(String name, String description, Boolean enabled) {
        this.name = name;
        this.description = description;
        this.enabled = enabled;
    }
}
