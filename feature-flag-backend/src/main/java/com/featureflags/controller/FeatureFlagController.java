package com.featureflags.controller;

import com.featureflags.dto.CreateFlagRequest;
import com.featureflags.dto.FeatureFlagBatchResponse;
import com.featureflags.dto.FlagListResponse;
import com.featureflags.dto.FlagResponse;
import com.featureflags.dto.UpdateFlagRequest;
import com.featureflags.service.FeatureFlagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for feature flag management.
 */
@RestController
@RequestMapping("/flags")
@CrossOrigin(origins = "*")
@Tag(name = "Feature Flags", description = "API for managing feature flags")
@RequiredArgsConstructor
@Slf4j
public class FeatureFlagController {

        private final FeatureFlagService featureFlagService;

        /**
         * Get all feature flags with pagination.
         */
        @GetMapping
        @Operation(summary = "Get all feature flags", description = "Retrieve all feature flags with pagination")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved feature flags"),
                        @ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
        })
        public ResponseEntity<FlagListResponse> getAllFlags(
                        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Page size(min 1, max 100)") @RequestParam(defaultValue = "20") int size) {

                log.debug("Getting all flags - page: {}, size: {}", page, size);

                if (page < 0) {
                        page = 0;
                }
                if (size <= 0 || size > 100) {
                        size = 20;
                }

                FlagListResponse response = featureFlagService.getAllFlags(page, size);
                return ResponseEntity.ok(response);
        }

        /**
         * Get a feature flag by name.
         */
        @GetMapping("/{name}")
        @Operation(summary = "Get feature flag by name", description = "Retrieve a specific feature flag by its name")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved feature flag"),
                        @ApiResponse(responseCode = "404", description = "Feature flag not found")
        })
        public ResponseEntity<FlagResponse> getFlagByName(
                        @Parameter(description = "Feature flag name") @PathVariable String name) {

                log.debug("Getting flag by name: {}", name);

                FlagResponse response = featureFlagService.getFlagByName(name);
                return ResponseEntity.ok(response);
        }

        /**
         * Create a new feature flag.
         */
        @PostMapping
        @Operation(summary = "Create feature flag", description = "Create a new feature flag")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Feature flag created successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid request data"),
                        @ApiResponse(responseCode = "409", description = "Feature flag already exists")
        })
        public ResponseEntity<FlagResponse> createFlag(
                        @Parameter(description = "Feature flag creation request") @Valid @RequestBody CreateFlagRequest request) {

                log.debug("Creating new flag: {}", request);

                FlagResponse response = featureFlagService.createFlag(request);
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        /**
         * Update an existing feature flag.
         */
        @PutMapping("/{name}")
        @Operation(summary = "Update feature flag", description = "Update an existing feature flag")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Feature flag updated successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid request data"),
                        @ApiResponse(responseCode = "404", description = "Feature flag not found")
        })
        public ResponseEntity<FlagResponse> updateFlag(
                        @Parameter(description = "Feature flag name") @PathVariable String name,
                        @Parameter(description = "Feature flag update request") @Valid @RequestBody UpdateFlagRequest request) {

                log.debug("Updating flag name: {} with request: {}", name, request);

                FlagResponse response = featureFlagService.updateFlag(name, request);
                return ResponseEntity.ok(response);
        }

        /**
         * Delete a feature flag.
         */
        @DeleteMapping("/{name}")
        @Operation(summary = "Delete feature flag", description = "Delete a feature flag")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Feature flag deleted successfully"),
                        @ApiResponse(responseCode = "404", description = "Feature flag not found")
        })
        public ResponseEntity<Void> deleteFlag(
                        @Parameter(description = "Feature flag name") @PathVariable String name) {

                log.debug("Deleting flag name: {}", name);

                featureFlagService.deleteFlag(name);
                return ResponseEntity.noContent().build();
        }

        /**
         * Get multiple feature flags with timestamp information for consistency
         * checking.
         * This endpoint provides atomic access to prevent race conditions during Redis
         * pub/sub processing.
         */
        @PostMapping("/batch")
        @Operation(summary = "Get feature flags batch with timestamps", description = "Get status of multiple feature flags with timestamp information for consistency checking")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved feature flags status with timestamps"),
                        @ApiResponse(responseCode = "400", description = "Invalid request data")
        })
        public ResponseEntity<FeatureFlagBatchResponse> getFeatureFlagsBatch(
                        @Parameter(description = "List of feature flag names") @RequestBody List<String> flagNames) {

                log.debug("Getting batch feature flags with timestamps: {}", flagNames);

                FeatureFlagBatchResponse result = featureFlagService.getFeatureFlagsBatch(flagNames);
                return ResponseEntity.ok(result);
        }

}
