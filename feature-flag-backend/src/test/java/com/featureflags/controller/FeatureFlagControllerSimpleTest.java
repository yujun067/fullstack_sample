package com.featureflags.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.featureflags.dto.CreateFlagRequest;
import com.featureflags.dto.FlagListResponse;
import com.featureflags.dto.FlagResponse;
import com.featureflags.dto.UpdateFlagRequest;
import com.featureflags.entity.FeatureFlag;
import com.featureflags.service.FeatureFlagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Simplified Controller test that doesn't rely on Spring context loading.
 * This approach uses MockMvcBuilders to manually set up the test environment.
 */
@ExtendWith(MockitoExtension.class)
class FeatureFlagControllerSimpleTest {

    private MockMvc mockMvc;

    @Mock
    private FeatureFlagService featureFlagService;

    @InjectMocks
    private FeatureFlagController featureFlagController;

    private ObjectMapper objectMapper;
    private FeatureFlag testFlag;
    private CreateFlagRequest createRequest;
    private UpdateFlagRequest updateRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(featureFlagController).build();
        objectMapper = new ObjectMapper();

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
    void testGetAllFlags_Success() throws Exception {
        // Given
        List<FlagResponse> flags = Arrays.asList(
                new FlagResponse(testFlag),
                new FlagResponse(new FeatureFlag()));
        FlagListResponse response = new FlagListResponse(flags, 2L, 0, 20);
        when(featureFlagService.getAllFlags(anyInt(), anyInt())).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/flags")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flags").isArray())
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20));
    }

    @Test
    void testGetFlagByName_Success() throws Exception {
        // Given
        FlagResponse response = new FlagResponse(testFlag);
        when(featureFlagService.getFlagByName("test_flag")).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/flags/test_flag"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("test_flag"))
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    void testCreateFlag_Success() throws Exception {
        // Given
        FlagResponse response = new FlagResponse(testFlag);
        when(featureFlagService.createFlag(any(CreateFlagRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/flags")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("test_flag"))
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    void testCreateFlag_ValidationError() throws Exception {
        // Given
        CreateFlagRequest invalidRequest = new CreateFlagRequest();
        invalidRequest.setName(""); // Invalid: empty name

        // When & Then
        mockMvc.perform(post("/flags")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateFlag_Success() throws Exception {
        // Given
        FlagResponse response = new FlagResponse(testFlag);
        when(featureFlagService.updateFlag(anyString(), any(UpdateFlagRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(put("/flags/test_flag")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("test_flag"));
    }

    @Test
    void testDeleteFlag_Success() throws Exception {
        // When & Then
        mockMvc.perform(delete("/flags/test_flag"))
                .andExpect(status().isNoContent());
    }
}
