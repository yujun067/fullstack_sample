package com.featureflags.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new feature flag.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateFlagRequest {

    @NotBlank(message = "Flag name is required")
    @Size(min = 1, max = 100, message = "Flag name must be between 1 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Flag name can only contain letters, numbers, and underscores")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotNull(message = "Enabled status is required")
    private Boolean enabled;
}
