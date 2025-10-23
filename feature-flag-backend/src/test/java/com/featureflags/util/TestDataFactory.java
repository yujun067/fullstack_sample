package com.featureflags.util;

import com.featureflags.dto.CreateFlagRequest;
import com.featureflags.dto.UpdateFlagRequest;
import com.featureflags.entity.FeatureFlag;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Factory class for creating test data objects.
 * Provides methods to create various test entities with sensible defaults.
 */
public class TestDataFactory {

    /**
     * Creates a basic FeatureFlag entity for testing.
     */
    public static FeatureFlag createFeatureFlag() {
        return createFeatureFlag("test_flag", "Test flag description", true);
    }

    /**
     * Creates a FeatureFlag entity with custom values.
     */
    public static FeatureFlag createFeatureFlag(String name, String description, boolean enabled) {
        FeatureFlag flag = new FeatureFlag();
        flag.setName(name);
        flag.setDescription(description);
        flag.setEnabled(enabled);
        flag.setCreatedAt(LocalDateTime.now());
        flag.setUpdatedAt(LocalDateTime.now());
        flag.setCreatedBy("test");
        flag.setUpdatedBy("test");
        return flag;
    }

    /**
     * Creates a CreateFlagRequest for testing.
     */
    public static CreateFlagRequest createCreateFlagRequest() {
        return createCreateFlagRequest("new_flag", "New flag description", false);
    }

    /**
     * Creates a CreateFlagRequest with custom values.
     */
    public static CreateFlagRequest createCreateFlagRequest(String name, String description, boolean enabled) {
        CreateFlagRequest request = new CreateFlagRequest();
        request.setName(name);
        request.setDescription(description);
        request.setEnabled(enabled);
        return request;
    }

    /**
     * Creates an UpdateFlagRequest for testing.
     */
    public static UpdateFlagRequest createUpdateFlagRequest() {
        return createUpdateFlagRequest("Updated description", false);
    }

    /**
     * Creates an UpdateFlagRequest with custom values.
     */
    public static UpdateFlagRequest createUpdateFlagRequest(String description, Boolean enabled) {
        UpdateFlagRequest request = new UpdateFlagRequest();
        request.setDescription(description);
        request.setEnabled(enabled);
        return request;
    }

    /**
     * Creates a list of FeatureFlag entities for testing.
     */
    public static List<FeatureFlag> createFeatureFlagList(int count) {
        List<FeatureFlag> flags = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            flags.add(createFeatureFlag(
                    "test_flag_" + i,
                    "Test flag " + i + " description",
                    i % 2 == 0));
        }
        return flags;
    }

    /**
     * Creates a FeatureFlag with maintenance mode settings.
     */
    public static FeatureFlag createMaintenanceModeFlag() {
        return createFeatureFlag("maintenance_mode", "Maintenance mode flag", false);
    }

    /**
     * Creates a FeatureFlag with dark mode settings.
     */
    public static FeatureFlag createDarkModeFlag() {
        return createFeatureFlag("dark_mode", "Dark mode flag", false);
    }

    /**
     * Creates a FeatureFlag with a very long description for testing validation.
     */
    public static FeatureFlag createFlagWithLongDescription() {
        StringBuilder longDescription = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longDescription.append("This is a very long description. ");
        }

        return createFeatureFlag("long_desc_flag", longDescription.toString(), true);
    }

    /**
     * Creates a FeatureFlag with special characters in the name.
     */
    public static FeatureFlag createFlagWithSpecialCharacters() {
        return createFeatureFlag("flag-with-special_chars.123", "Flag with special characters", true);
    }

    /**
     * Creates a FeatureFlag with minimal data for testing edge cases.
     */
    public static FeatureFlag createMinimalFlag() {
        FeatureFlag flag = new FeatureFlag();
        flag.setName("minimal");
        flag.setEnabled(false);
        return flag;
    }

    /**
     * Creates a FeatureFlag with maximum length name for testing validation.
     */
    public static FeatureFlag createFlagWithMaxLengthName() {
        StringBuilder maxName = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            maxName.append("a");
        }

        return createFeatureFlag(maxName.toString(), "Flag with maximum length name", true);
    }
}
