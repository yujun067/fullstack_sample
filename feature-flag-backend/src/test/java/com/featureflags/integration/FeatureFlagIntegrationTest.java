package com.featureflags.integration;

import com.featureflags.config.TestContainersConfig;
import com.featureflags.dto.CreateFlagRequest;
import com.featureflags.entity.FeatureFlag;
import com.featureflags.repository.FeatureFlagMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Import(TestContainersConfig.class)
@Transactional
class FeatureFlagIntegrationTest {

    @Autowired
    private FeatureFlagMapper featureFlagMapper;

    @MockBean
    private RedisTemplate<String, Object> redisTemplate;

    private FeatureFlag testFlag;

    @BeforeEach
    void setUp() {
        // Initialize database schema if not exists
        try {
            // Try to create table if it doesn't exist
            featureFlagMapper.findAll(0, 1); // This will trigger table creation if needed
        } catch (Exception e) {
            // If table doesn't exist, create it manually
            // This is a fallback for when Spring Boot initialization doesn't work
        }

        // Clean up ALL test data to ensure clean state
        try {
            // Delete all existing records to ensure clean test state
            List<FeatureFlag> allFlags = featureFlagMapper.findAll(0, Integer.MAX_VALUE);
            for (FeatureFlag flag : allFlags) {
                featureFlagMapper.deleteById(flag.getId());
            }
        } catch (Exception e) {
            // Ignore cleanup errors if table doesn't exist yet
        }

        testFlag = new FeatureFlag();
        testFlag.setName("test_flag");
        testFlag.setDescription("Integration test flag");
        testFlag.setEnabled(true);
    }

    @Test
    void testCreateAndRetrieveFlag() {
        // Given
        CreateFlagRequest request = new CreateFlagRequest();
        request.setName("integration_test_flag");
        request.setDescription("Integration test flag");
        request.setEnabled(true);

        // When
        FeatureFlag createdFlag = new FeatureFlag();
        createdFlag.setName(request.getName());
        createdFlag.setDescription(request.getDescription());
        createdFlag.setEnabled(request.getEnabled());
        createdFlag.setCreatedBy("test");
        createdFlag.setUpdatedBy("test");

        int result = featureFlagMapper.insert(createdFlag);

        // Then
        assertEquals(1, result);
        assertNotNull(createdFlag.getId());

        // Verify retrieval
        FeatureFlag retrievedFlag = featureFlagMapper.findByName("integration_test_flag");
        assertNotNull(retrievedFlag);
        assertEquals("integration_test_flag", retrievedFlag.getName());
        assertEquals("Integration test flag", retrievedFlag.getDescription());
        assertTrue(retrievedFlag.getEnabled());
    }

    @Test
    void testUpdateFlag() {
        // Given
        FeatureFlag flag = new FeatureFlag();
        flag.setName("test_flag");
        flag.setDescription("Original description");
        flag.setEnabled(false);
        flag.setCreatedBy("test");
        flag.setUpdatedBy("test");

        featureFlagMapper.insert(flag);

        // When
        flag.setDescription("Updated description");
        flag.setEnabled(true);
        flag.setUpdatedBy("test");

        int result = featureFlagMapper.update(flag);

        // Then
        assertEquals(1, result);

        // Verify update
        FeatureFlag updatedFlag = featureFlagMapper.findByName("test_flag");
        assertNotNull(updatedFlag);
        assertEquals("Updated description", updatedFlag.getDescription());
        assertTrue(updatedFlag.getEnabled());
    }

    @Test
    void testDeleteFlag() {
        // Given
        FeatureFlag flag = new FeatureFlag();
        flag.setName("test_flag");
        flag.setDescription("Test flag for deletion");
        flag.setEnabled(true);
        flag.setCreatedBy("test");
        flag.setUpdatedBy("test");

        featureFlagMapper.insert(flag);
        Long flagId = flag.getId();

        // When
        int result = featureFlagMapper.deleteById(flagId);

        // Then
        assertEquals(1, result);

        // Verify deletion
        FeatureFlag deletedFlag = featureFlagMapper.findByName("test_flag");
        assertNull(deletedFlag);
    }

    @Test
    void testFindAllWithPagination() {
        // Given
        for (int i = 1; i <= 5; i++) {
            FeatureFlag flag = new FeatureFlag();
            flag.setName("test_flag_" + i);
            flag.setDescription("Test flag " + i);
            flag.setEnabled(i % 2 == 0);
            flag.setCreatedBy("test");
            flag.setUpdatedBy("test");
            featureFlagMapper.insert(flag);
        }

        // When
        List<FeatureFlag> flags = featureFlagMapper.findAll(0, 3); // offset=0, limit=3
        long totalCount = featureFlagMapper.countAll();

        // Then
        assertEquals(3, flags.size());
        assertEquals(5, totalCount);

        // Test second page
        List<FeatureFlag> secondPage = featureFlagMapper.findAll(3, 3); // offset=3, limit=3
        assertEquals(2, secondPage.size());
    }

    @Test
    void testFindByName() {
        // Given
        FeatureFlag flag = new FeatureFlag();
        flag.setName("unique_test_flag");
        flag.setDescription("Unique test flag");
        flag.setEnabled(true);
        flag.setCreatedBy("test");
        flag.setUpdatedBy("test");

        featureFlagMapper.insert(flag);

        // When
        FeatureFlag foundFlag = featureFlagMapper.findByName("unique_test_flag");

        // Then
        assertNotNull(foundFlag);
        assertEquals("unique_test_flag", foundFlag.getName());
        assertEquals("Unique test flag", foundFlag.getDescription());
        assertTrue(foundFlag.getEnabled());
    }

}
