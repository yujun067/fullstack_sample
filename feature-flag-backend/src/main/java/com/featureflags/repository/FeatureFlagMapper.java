package com.featureflags.repository;

import com.featureflags.entity.FeatureFlag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * MyBatis mapper interface for FeatureFlag operations.
 */
@Mapper
public interface FeatureFlagMapper {

        /**
         * Find all feature flags with pagination.
         */
        List<FeatureFlag> findAll(@Param("offset") int offset, @Param("limit") int limit);

        /**
         * Find feature flag by ID.
         */
        FeatureFlag findById(@Param("id") Long id);

        /**
         * Find feature flag by name.
         */
        FeatureFlag findByName(@Param("name") String name);

        /**
         * Count total number of feature flags.
         */
        long countAll();

        /**
         * Insert a new feature flag.
         */
        int insert(FeatureFlag flag);

        /**
         * Update an existing feature flag.
         */
        int update(FeatureFlag flag);

        /**
         * Delete a feature flag by ID.
         */
        int deleteById(@Param("id") Long id);

        /**
         * Check if a feature flag exists by name.
         */
        boolean existsByName(@Param("name") String name);

        /**
         * Find feature flags by names (batch query).
         */
        List<FeatureFlag> findByNames(@Param("names") List<String> names);

}
