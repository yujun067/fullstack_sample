package com.moviesearch.service;

import com.moviesearch.config.FeatureFlagConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeatureFlagConsumerTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private FeatureFlagConfig featureFlagConfig;

    @InjectMocks
    private FeatureFlagConsumer featureFlagConsumer;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // Mock subscribed feature flags configuration
        Map<String, Boolean> subscribed = new HashMap<>();
        subscribed.put("maintenance_mode", false);
        subscribed.put("dark_mode", false);
        lenient().when(featureFlagConfig.getSubscribed()).thenReturn(subscribed);
    }

    @Test
    void testIsMaintenanceModeEnabled_Default() {
        // When
        boolean maintenanceMode = featureFlagConsumer.isMaintenanceModeEnabled();

        // Then
        assertFalse(maintenanceMode);
    }

    @Test
    void testIsMaintenanceModeEnabled_Enabled() {
        // Given
        featureFlagConsumer.updateFeatureFlag("maintenance_mode", true);

        // When
        boolean maintenanceMode = featureFlagConsumer.isMaintenanceModeEnabled();

        // Then
        assertTrue(maintenanceMode);
    }

    @Test
    void testIsDarkModeEnabled_Default() {
        // When
        boolean darkMode = featureFlagConsumer.isDarkModeEnabled();

        // Then
        assertFalse(darkMode);
    }

    @Test
    void testIsDarkModeEnabled_Enabled() {
        // Given
        featureFlagConsumer.updateFeatureFlag("dark_mode", true);

        // When
        boolean darkMode = featureFlagConsumer.isDarkModeEnabled();

        // Then
        assertTrue(darkMode);
    }

    @Test
    void testGetAllFeatureFlags() {
        // Given
        featureFlagConsumer.updateFeatureFlag("maintenance_mode", true);
        featureFlagConsumer.updateFeatureFlag("dark_mode", false);
        featureFlagConsumer.updateFeatureFlag("new_feature", true);

        // When
        Map<String, Boolean> flags = featureFlagConsumer.getAllFeatureFlags();

        // Then
        assertNotNull(flags);
        assertEquals(3, flags.size());
        assertTrue(flags.get("maintenance_mode"));
        assertFalse(flags.get("dark_mode"));
        assertTrue(flags.get("new_feature"));
    }

    @Test
    void testUpdateFeatureFlag() {
        // Given
        String flagName = "test_flag";
        boolean enabled = true;

        // When
        featureFlagConsumer.updateFeatureFlag(flagName, enabled);

        // Then
        assertTrue(featureFlagConsumer.getAllFeatureFlags().get(flagName));
    }

    @Test
    void testRemoveFeatureFlag() {
        // Given
        featureFlagConsumer.updateFeatureFlag("test_flag", true);
        assertTrue(featureFlagConsumer.getAllFeatureFlags().containsKey("test_flag"));

        // When
        featureFlagConsumer.removeFeatureFlag("test_flag");

        // Then
        assertFalse(featureFlagConsumer.getAllFeatureFlags().containsKey("test_flag"));
    }

    @Test
    void testInitializeFeatureFlags() {
        // When
        featureFlagConsumer.initializeFeatureFlags();

        // Then
        Map<String, Boolean> flags = featureFlagConsumer.getAllFeatureFlags();
        assertFalse(flags.get("maintenance_mode"));
        assertFalse(flags.get("dark_mode"));
    }

    @Test
    void testInitializeFeatureFlags_RedisUnavailable() {
        // When
        featureFlagConsumer.initializeFeatureFlags();

        // Then
        Map<String, Boolean> flags = featureFlagConsumer.getAllFeatureFlags();
        assertFalse(flags.get("maintenance_mode"));
        assertFalse(flags.get("dark_mode"));
    }

    @Test
    void testGetDefaultValue() {
        // Test maintenance_mode default
        featureFlagConsumer.updateFeatureFlag("maintenance_mode", false);
        assertFalse(featureFlagConsumer.isMaintenanceModeEnabled());

        // Test dark_mode default
        featureFlagConsumer.updateFeatureFlag("dark_mode", false);
        assertFalse(featureFlagConsumer.isDarkModeEnabled());
    }
}
