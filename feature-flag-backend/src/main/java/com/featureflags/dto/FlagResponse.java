package com.featureflags.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.featureflags.entity.FeatureFlag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for feature flag response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlagResponse implements Serializable {

    private Long id;
    private String name;
    private String description;
    private Boolean enabled;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    private String createdBy;
    private String updatedBy;

    // Constructor from entity
    public FlagResponse(FeatureFlag flag) {
        this.id = flag.getId();
        this.name = flag.getName();
        this.description = flag.getDescription();
        this.enabled = flag.getEnabled();
        this.createdAt = flag.getCreatedAt();
        this.updatedAt = flag.getUpdatedAt();
        this.createdBy = flag.getCreatedBy();
        this.updatedBy = flag.getUpdatedBy();
    }
}
