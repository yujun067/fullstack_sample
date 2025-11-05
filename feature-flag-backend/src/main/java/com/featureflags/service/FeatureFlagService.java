package com.featureflags.service;

import com.featureflags.dto.CreateFlagRequest;
import com.featureflags.dto.FeatureFlagBatchResponse;
import com.featureflags.dto.FlagListResponse;
import com.featureflags.dto.FlagResponse;
import com.featureflags.dto.UpdateFlagRequest;
import com.featureflags.entity.FeatureFlag;
import com.featureflags.exception.FlagNotFoundException;
import com.featureflags.exception.FlagAlreadyExistsException;
import com.featureflags.repository.FeatureFlagMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service class for managing feature flags.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class FeatureFlagService {

    private final FeatureFlagMapper featureFlagMapper;
    private final MessagePublisherService messagePublisherService;

    /**
     * Get all feature flags with pagination.
     */
    @Transactional(readOnly = true)
    public FlagListResponse getAllFlags(int page, int size) {
        log.debug("Getting all flags - page: {}, size: {}", page, size);

        int offset = page * size;
        List<FeatureFlag> flags = featureFlagMapper.findAll(offset, size);
        long total = featureFlagMapper.countAll();

        List<FlagResponse> flagResponses = flags.stream()
                .map(FlagResponse::new)
                .collect(Collectors.toList());

        return new FlagListResponse(flagResponses, total, page, size);
    }

    /**
     * Get a feature flag by name.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "featureFlags", key = "#name")
    public FlagResponse getFlagByName(String name) {
        log.debug("Getting flag by name: {}", name);

        FeatureFlag flag = featureFlagMapper.findByName(name);
        if (flag == null) {
            throw new FlagNotFoundException("Feature flag not found with name: " + name);
        }

        return new FlagResponse(flag);
    }

    /**
     * Create a new feature flag.
     */
    @CacheEvict(value = "featureFlags", key = "#request.name")
    public FlagResponse createFlag(CreateFlagRequest request) {
        log.debug("Creating new flag: {}", request);

        // Check if flag with same name already exists
        if (featureFlagMapper.existsByName(request.getName())) {
            throw new FlagAlreadyExistsException("Feature flag already exists with name: " + request.getName());
        }

        FeatureFlag flag = new FeatureFlag();
        flag.setName(request.getName());
        flag.setDescription(request.getDescription());
        flag.setEnabled(request.getEnabled());
        flag.setCreatedBy("system"); // In real app, get from security context
        flag.setUpdatedBy("system");

        int result = featureFlagMapper.insert(flag);
        if (result == 0) {
            throw new RuntimeException("Failed to create feature flag");
        }

        log.info("Created feature flag: {}", flag);

        // Publish flag creation event
        messagePublisherService.publishFlagCreated(flag);

        return new FlagResponse(flag);
    }

    /**
     * Update an existing feature flag.
     */
    @CacheEvict(value = "featureFlags", key = "#name")
    public FlagResponse updateFlag(String name, UpdateFlagRequest request) {
        log.debug("Updating flag name: {} with request: {}", name, request);

        FeatureFlag existingFlag = featureFlagMapper.findByName(name);
        if (existingFlag == null) {
            throw new FlagNotFoundException("Feature flag not found with name: " + name);
        }

        // Update fields if provided
        if (request.getDescription() != null) {
            existingFlag.setDescription(request.getDescription());
        }
        if (request.getEnabled() != null) {
            existingFlag.setEnabled(request.getEnabled());
        }
        existingFlag.setUpdatedBy("system"); // In real app, get from security context

        int result = featureFlagMapper.update(existingFlag);
        if (result == 0) {
            throw new RuntimeException("Failed to update feature flag");
        }

        log.info("Updated feature flag: {}", existingFlag);

        // Publish flag update event
        messagePublisherService.publishFlagUpdated(existingFlag);

        return new FlagResponse(existingFlag);
    }

    /**
     * Delete a feature flag.
     */
    @CacheEvict(value = "featureFlags", key = "#name")
    public void deleteFlag(String name) {
        log.debug("Deleting flag name: {}", name);

        FeatureFlag flag = featureFlagMapper.findByName(name);
        if (flag == null) {
            throw new FlagNotFoundException("Feature flag not found with name: " + name);
        }

        int result = featureFlagMapper.deleteById(flag.getId());
        if (result == 0) {
            throw new RuntimeException("Failed to delete feature flag");
        }

        log.info("Deleted feature flag: {}", flag);

        // Publish flag deletion event
        messagePublisherService.publishFlagDeleted(flag);
    }

    /**
     * Get multiple feature flags with timestamp information for consistency
     * checking.
     */
    @Transactional(readOnly = true)
    public FeatureFlagBatchResponse getFeatureFlagsBatch(List<String> flagNames) {
        log.debug("Getting batch feature flags with timestamps: {}", flagNames);

        if (flagNames == null || flagNames.isEmpty()) {
            return FeatureFlagBatchResponse.builder()
                    .flags(Map.of())
                    .responseTimestamp(java.time.LocalDateTime.now())
                    .build();
        }

        try {
            // Get atomic snapshot of all requested flags
            List<FeatureFlag> flags = featureFlagMapper.findByNames(flagNames);

            // Build response with timestamp information
            Map<String, FeatureFlagBatchResponse.FeatureFlagInfo> flagsWithTimestamps = new java.util.HashMap<>();
            for (FeatureFlag flag : flags) {
                FeatureFlagBatchResponse.FeatureFlagInfo flagInfo = FeatureFlagBatchResponse.FeatureFlagInfo.builder()
                        .enabled(flag.getEnabled())
                        .timestamp(flag.getUpdatedAt())
                        .name(flag.getName())
                        .description(flag.getDescription())
                        .build();
                flagsWithTimestamps.put(flag.getName(), flagInfo);
            }

            return FeatureFlagBatchResponse.builder()
                    .flags(flagsWithTimestamps)
                    .responseTimestamp(java.time.LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Failed to get batch feature flags with timestamps: {}", e.getMessage());
            throw new RuntimeException("Failed to retrieve feature flags", e);
        }
    }

}
