package com.featureflags.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating an existing feature flag.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFlagRequest {

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    private Boolean enabled;
}
