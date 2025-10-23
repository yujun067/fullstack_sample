package com.featureflags.service;

import com.featureflags.config.UnitTestConfig;
import com.featureflags.dto.CreateFlagRequest;
import com.featureflags.dto.FlagListResponse;
import com.featureflags.dto.FlagResponse;
import com.featureflags.dto.UpdateFlagRequest;
import com.featureflags.entity.FeatureFlag;
import com.featureflags.exception.FlagAlreadyExistsException;
import com.featureflags.exception.FlagNotFoundException;
import com.featureflags.repository.FeatureFlagMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Import(UnitTestConfig.class)
class FeatureFlagServiceTest {

    @Mock
    private FeatureFlagMapper featureFlagMapper;

    @Mock
    private MessagePublisherService messagePublisherService;

    @InjectMocks
    private FeatureFlagService featureFlagService;

    private FeatureFlag testFlag;
    private CreateFlagRequest createRequest;
    private UpdateFlagRequest updateRequest;

    @BeforeEach
    void setUp() {
        testFlag = new FeatureFlag();
        testFlag.setId(1L);
        testFlag.setName("test_flag");
        testFlag.setDescription("A test feature flag");
        testFlag.setEnabled(true);
        testFlag.setCreatedAt(LocalDateTime.now());
        testFlag.setUpdatedAt(LocalDateTime.now());

        createRequest = new CreateFlagRequest();
        createRequest.setName("new_flag");
        createRequest.setDescription("A new feature flag");
        createRequest.setEnabled(false);

        updateRequest = new UpdateFlagRequest();
        updateRequest.setDescription("Updated description");
        updateRequest.setEnabled(false);
    }

    @Test
    void testGetAllFlags_Success() {
        // Given
        List<FeatureFlag> flags = Arrays.asList(testFlag, new FeatureFlag());
        when(featureFlagMapper.findAll(0, 10)).thenReturn(flags);
        when(featureFlagMapper.countAll()).thenReturn(2L);

        // When
        FlagListResponse response = featureFlagService.getAllFlags(0, 10);

        // Then
        assertNotNull(response);
        assertEquals(2, response.getFlags().size());
        assertEquals(2L, response.getTotal());
        verify(featureFlagMapper).findAll(0, 10);
        verify(featureFlagMapper).countAll();
    }

    @Test
    void testGetFlagByName_Success() {
        // Given
        when(featureFlagMapper.findByName("test_flag")).thenReturn(testFlag);

        // When
        FlagResponse response = featureFlagService.getFlagByName("test_flag");

        // Then
        assertNotNull(response);
        assertEquals("test_flag", response.getName());
        verify(featureFlagMapper).findByName("test_flag");
    }

    @Test
    void testGetFlagByName_NotFound() {
        // Given
        when(featureFlagMapper.findByName("nonexistent_flag")).thenReturn(null);

        // When & Then
        assertThrows(FlagNotFoundException.class, () -> {
            featureFlagService.getFlagByName("nonexistent_flag");
        });
    }

    @Test
    void testCreateFlag_Success() {
        // Given
        when(featureFlagMapper.existsByName(createRequest.getName())).thenReturn(false);
        when(featureFlagMapper.insert(any(FeatureFlag.class))).thenReturn(1);

        // When
        FlagResponse response = featureFlagService.createFlag(createRequest);

        // Then
        assertNotNull(response);
        assertEquals(createRequest.getName(), response.getName());
        verify(featureFlagMapper).existsByName(createRequest.getName());
        verify(featureFlagMapper).insert(any(FeatureFlag.class));
        verify(messagePublisherService).publishFlagCreated(any(FeatureFlag.class));
    }

    @Test
    void testCreateFlag_AlreadyExists() {
        // Given
        when(featureFlagMapper.existsByName("new_flag")).thenReturn(true);

        // When & Then
        assertThrows(FlagAlreadyExistsException.class, () -> {
            featureFlagService.createFlag(createRequest);
        });

        verify(featureFlagMapper).existsByName("new_flag");
        verify(featureFlagMapper, never()).insert(any(FeatureFlag.class));
    }

    @Test
    void testUpdateFlag_Success() {
        // Given
        when(featureFlagMapper.findByName("test_flag")).thenReturn(testFlag);
        when(featureFlagMapper.update(any(FeatureFlag.class))).thenReturn(1);

        // When
        FlagResponse response = featureFlagService.updateFlag("test_flag", updateRequest);

        // Then
        assertNotNull(response);
        assertEquals(updateRequest.getDescription(), response.getDescription());
        assertEquals(updateRequest.getEnabled(), response.getEnabled());

        verify(featureFlagMapper).findByName("test_flag");
        verify(featureFlagMapper).update(any(FeatureFlag.class));
        verify(messagePublisherService).publishFlagUpdated(any(FeatureFlag.class));
    }

    @Test
    void testUpdateFlag_NotFound() {
        // Given
        when(featureFlagMapper.findByName("nonexistent_flag")).thenReturn(null);

        // When & Then
        assertThrows(FlagNotFoundException.class, () -> {
            featureFlagService.updateFlag("nonexistent_flag", updateRequest);
        });

        verify(featureFlagMapper).findByName("nonexistent_flag");
        verify(featureFlagMapper, never()).update(any(FeatureFlag.class));
    }

    @Test
    void testDeleteFlag_Success() {
        // Given
        when(featureFlagMapper.findByName("test_flag")).thenReturn(testFlag);
        when(featureFlagMapper.deleteById(1L)).thenReturn(1);

        // When
        featureFlagService.deleteFlag("test_flag");

        // Then
        verify(featureFlagMapper).findByName("test_flag");
        verify(featureFlagMapper).deleteById(1L);
        verify(messagePublisherService).publishFlagDeleted(any(FeatureFlag.class));
    }

    @Test
    void testDeleteFlag_NotFound() {
        // Given
        when(featureFlagMapper.findByName("nonexistent_flag")).thenReturn(null);

        // When & Then
        assertThrows(FlagNotFoundException.class, () -> {
            featureFlagService.deleteFlag("nonexistent_flag");
        });

        verify(featureFlagMapper).findByName("nonexistent_flag");
        verify(featureFlagMapper, never()).deleteById(anyLong());
    }
}
