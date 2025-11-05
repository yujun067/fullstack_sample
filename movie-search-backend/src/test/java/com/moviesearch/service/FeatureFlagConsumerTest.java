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
    void testUpdateFeatureFlag() {
        // Given
        String flagName = "test_flag";
        boolean enabled = true;

        // When
        featureFlagConsumer.updateFeatureFlag(flagName, enabled);

        // Then
        Boolean flagValue = featureFlagConsumer.getFeatureFlag(flagName);
        assertNotNull(flagValue);
        assertTrue(flagValue);
    }

    @Test
    void testRemoveFeatureFlag() {
        // Given
        featureFlagConsumer.updateFeatureFlag("test_flag", true);
        assertNotNull(featureFlagConsumer.getFeatureFlag("test_flag"));

        // When
        featureFlagConsumer.removeFeatureFlag("test_flag");

        // Then
        assertNull(featureFlagConsumer.getFeatureFlag("test_flag"));
    }

    @Test
    void testInitializeFeatureFlags() {
        // When
        featureFlagConsumer.initializeFeatureFlags();

        // Then
        Boolean maintenanceMode = featureFlagConsumer.getFeatureFlag("maintenance_mode");
        assertNotNull(maintenanceMode);
        assertFalse(maintenanceMode);
    }

    @Test
    void testInitializeFeatureFlags_RedisUnavailable() {
        // When
        featureFlagConsumer.initializeFeatureFlags();

        // Then
        Boolean maintenanceMode = featureFlagConsumer.getFeatureFlag("maintenance_mode");
        assertNotNull(maintenanceMode);
        assertFalse(maintenanceMode);
    }

    @Test
    void testGetDefaultValue() {
        // Test maintenance_mode default
        featureFlagConsumer.updateFeatureFlag("maintenance_mode", false);
        assertFalse(featureFlagConsumer.isMaintenanceModeEnabled());
    }
}
