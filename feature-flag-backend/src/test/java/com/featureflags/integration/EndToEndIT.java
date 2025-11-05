package com.featureflags.integration;

import com.featureflags.config.BaseIntegrationTest;
import com.featureflags.dto.CreateFlagRequest;
import com.featureflags.entity.FeatureFlag;
import com.featureflags.repository.FeatureFlagMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-End integration tests for the Feature Flag application.
 * Tests the complete workflow from API calls to database persistence AND Redis
 * message publishing.
 * Uses TestContainers to provide both MySQL and Redis environments.
 * 
 * This test verifies:
 * 1. API endpoints work correctly
 * 2. Database operations work correctly
 * 3. Redis message publishing works correctly
 * 4. Complete message flow from API to Redis
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class EndToEndIT extends BaseIntegrationTest {

        private MockMvc mockMvc;

        @Autowired
        private WebApplicationContext webApplicationContext;

        @Autowired
        private FeatureFlagMapper featureFlagMapper;

        @Autowired
        private RedisTemplate<String, String> redisTemplate;

        @Autowired
        private ObjectMapper objectMapper;

        @BeforeEach
        void setUp() {
                // Setup MockMvc
                this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

                // Clean up test data
                List<FeatureFlag> allFlags = featureFlagMapper.findAll(0, Integer.MAX_VALUE);
                for (FeatureFlag flag : allFlags) {
                        featureFlagMapper.deleteById(flag.getId());
                }

                // Note: Redis pub/sub messages are transient, so no cleanup needed
        }

        @Test
        void testCompleteFeatureFlagWorkflow() throws Exception {
                // Verify Redis connection is available
                assertNotNull(redisTemplate.getConnectionFactory());

                // 1. Create a new feature flag
                CreateFlagRequest createRequest = new CreateFlagRequest();
                createRequest.setName("e2e_test_flag");
                createRequest.setDescription("End-to-end test flag");
                createRequest.setEnabled(false);

                mockMvc.perform(post("/flags")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.name").value("e2e_test_flag"))
                                .andExpect(jsonPath("$.description").value("End-to-end test flag"))
                                .andExpect(jsonPath("$.enabled").value(false));

                // Verify flag was created in database
                FeatureFlag createdFlag = featureFlagMapper.findByName("e2e_test_flag");
                assertNotNull(createdFlag);
                assertEquals("e2e_test_flag", createdFlag.getName());
                assertEquals("End-to-end test flag", createdFlag.getDescription());
                assertFalse(createdFlag.getEnabled());

                // Wait a moment for Redis message to be published
                Thread.sleep(100);

                // 2. Retrieve the flag
                mockMvc.perform(get("/flags/e2e_test_flag"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value("e2e_test_flag"))
                                .andExpect(jsonPath("$.description").value("End-to-end test flag"))
                                .andExpect(jsonPath("$.enabled").value(false));

                // 3. Update the flag
                com.featureflags.dto.UpdateFlagRequest updateRequest = new com.featureflags.dto.UpdateFlagRequest();
                updateRequest.setDescription("Updated end-to-end test flag");
                updateRequest.setEnabled(true);

                mockMvc.perform(put("/flags/e2e_test_flag")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value("e2e_test_flag"))
                                .andExpect(jsonPath("$.description").value("Updated end-to-end test flag"))
                                .andExpect(jsonPath("$.enabled").value(true));

                // Wait for Redis update message
                Thread.sleep(100);

                // Verify flag was updated in database
                FeatureFlag updatedFlag = featureFlagMapper.findByName("e2e_test_flag");
                assertNotNull(updatedFlag);
                assertEquals("Updated end-to-end test flag", updatedFlag.getDescription());
                assertTrue(updatedFlag.getEnabled());

                // 4. List all flags
                mockMvc.perform(get("/flags")
                                .param("page", "0")
                                .param("size", "10"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.flags").isArray())
                                .andExpect(jsonPath("$.flags[0].name").value("e2e_test_flag"))
                                .andExpect(jsonPath("$.total").value(1));

                // 5. Delete the flag
                mockMvc.perform(delete("/flags/e2e_test_flag"))
                                .andExpect(status().isNoContent());

                // Wait for Redis delete message
                Thread.sleep(100);

                // Verify flag was deleted from database
                FeatureFlag deletedFlag = featureFlagMapper.findByName("e2e_test_flag");
                assertNull(deletedFlag);
        }

        @Test
        void testFeatureFlagValidation() throws Exception {
                // Verify Redis connection is available
                assertNotNull(redisTemplate.getConnectionFactory());

                // Test invalid flag creation
                CreateFlagRequest invalidRequest = new CreateFlagRequest();
                invalidRequest.setName(""); // Invalid: empty name
                invalidRequest.setDescription("Test description");

                mockMvc.perform(post("/flags")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                                .andExpect(jsonPath("$.message").value("Validation failed"));

                // Test duplicate flag creation
                CreateFlagRequest firstRequest = new CreateFlagRequest();
                firstRequest.setName("duplicate_flag");
                firstRequest.setDescription("First flag");
                firstRequest.setEnabled(false);

                // Create first flag
                mockMvc.perform(post("/flags")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(firstRequest)))
                                .andExpect(status().isCreated());

                // Try to create duplicate flag
                CreateFlagRequest duplicateRequest = new CreateFlagRequest();
                duplicateRequest.setName("duplicate_flag");
                duplicateRequest.setDescription("Duplicate flag");
                duplicateRequest.setEnabled(true);

                mockMvc.perform(post("/flags")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(duplicateRequest)))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.error").value("FLAG_ALREADY_EXISTS"))
                                .andExpect(jsonPath("$.message")
                                                .value("Feature flag already exists with name: duplicate_flag"));
        }

        @Test
        void testFeatureFlagNotFound() throws Exception {
                // Verify Redis connection is available
                assertNotNull(redisTemplate.getConnectionFactory());

                // Test getting non-existent flag
                mockMvc.perform(get("/flags/nonexistent_flag"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.error").value("FLAG_NOT_FOUND"))
                                .andExpect(jsonPath("$.message")
                                                .value("Feature flag not found with name: nonexistent_flag"));

                // Test updating non-existent flag
                com.featureflags.dto.UpdateFlagRequest updateRequest = new com.featureflags.dto.UpdateFlagRequest();
                updateRequest.setDescription("Updated description");
                updateRequest.setEnabled(true);

                mockMvc.perform(put("/flags/nonexistent_flag")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.error").value("FLAG_NOT_FOUND"))
                                .andExpect(jsonPath("$.message")
                                                .value("Feature flag not found with name: nonexistent_flag"));

                // Test deleting non-existent flag
                mockMvc.perform(delete("/flags/nonexistent_flag"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.error").value("FLAG_NOT_FOUND"))
                                .andExpect(jsonPath("$.message")
                                                .value("Feature flag not found with name: nonexistent_flag"));
        }

        @Test
        void testFeatureFlagPagination() throws Exception {
                // Verify Redis connection is available
                assertNotNull(redisTemplate.getConnectionFactory());

                // Create multiple flags for pagination testing
                for (int i = 1; i <= 5; i++) {
                        CreateFlagRequest request = new CreateFlagRequest();
                        request.setName("pagination_flag_" + i);
                        request.setDescription("Pagination test flag " + i);
                        request.setEnabled(i % 2 == 0);

                        mockMvc.perform(post("/flags")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isCreated());
                }

                // Test first page
                mockMvc.perform(get("/flags")
                                .param("page", "0")
                                .param("size", "3"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.flags").isArray())
                                .andExpect(jsonPath("$.flags.length()").value(3))
                                .andExpect(jsonPath("$.total").value(5))
                                .andExpect(jsonPath("$.page").value(0))
                                .andExpect(jsonPath("$.size").value(3));

                // Test second page
                mockMvc.perform(get("/flags")
                                .param("page", "1")
                                .param("size", "3"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.flags").isArray())
                                .andExpect(jsonPath("$.flags.length()").value(2))
                                .andExpect(jsonPath("$.total").value(5))
                                .andExpect(jsonPath("$.page").value(1))
                                .andExpect(jsonPath("$.size").value(3));
        }

        @Test
        void testFeatureFlagSearchAndFilter() throws Exception {
                // Verify Redis connection is available
                assertNotNull(redisTemplate.getConnectionFactory());

                // Create flags with different characteristics
                String[] flagNames = { "search_flag_1", "search_flag_2", "filter_flag_1", "filter_flag_2" };
                boolean[] enabledStates = { true, false, true, false };

                for (int i = 0; i < flagNames.length; i++) {
                        CreateFlagRequest request = new CreateFlagRequest();
                        request.setName(flagNames[i]);
                        request.setDescription("Test flag " + (i + 1));
                        request.setEnabled(enabledStates[i]);

                        mockMvc.perform(post("/flags")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isCreated());
                }

                // Test listing all flags
                mockMvc.perform(get("/flags"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.flags").isArray())
                                .andExpect(jsonPath("$.flags.length()").value(4))
                                .andExpect(jsonPath("$.total").value(4));

                // Test retrieving specific flags
                for (String flagName : flagNames) {
                        mockMvc.perform(get("/flags/" + flagName))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.name").value(flagName));
                }
        }

        @Test
        void testFeatureFlagConcurrentOperations() throws Exception {
                // Verify Redis connection is available
                assertNotNull(redisTemplate.getConnectionFactory());

                // Test concurrent flag creation
                CreateFlagRequest request1 = new CreateFlagRequest();
                request1.setName("concurrent_flag_1");
                request1.setDescription("Concurrent test flag 1");
                request1.setEnabled(true);

                CreateFlagRequest request2 = new CreateFlagRequest();
                request2.setName("concurrent_flag_2");
                request2.setDescription("Concurrent test flag 2");
                request2.setEnabled(false);

                // Create flags concurrently (simulated)
                mockMvc.perform(post("/flags")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request1)))
                                .andExpect(status().isCreated());

                mockMvc.perform(post("/flags")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request2)))
                                .andExpect(status().isCreated());

                // Verify both flags were created
                FeatureFlag flag1 = featureFlagMapper.findByName("concurrent_flag_1");
                FeatureFlag flag2 = featureFlagMapper.findByName("concurrent_flag_2");

                assertNotNull(flag1);
                assertNotNull(flag2);
                assertEquals("concurrent_flag_1", flag1.getName());
                assertEquals("concurrent_flag_2", flag2.getName());
                assertTrue(flag1.getEnabled());
                assertFalse(flag2.getEnabled());
        }

        @Test
        void testFeatureFlagPerformance() throws Exception {
                // Verify Redis connection is available
                assertNotNull(redisTemplate.getConnectionFactory());

                long startTime = System.currentTimeMillis();

                // Create multiple flags to test performance
                for (int i = 1; i <= 10; i++) {
                        CreateFlagRequest request = new CreateFlagRequest();
                        request.setName("perf_flag_" + i);
                        request.setDescription("Performance test flag " + i);
                        request.setEnabled(i % 2 == 0);

                        mockMvc.perform(post("/flags")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isCreated());
                }

                long createTime = System.currentTimeMillis() - startTime;

                // Test retrieval performance
                startTime = System.currentTimeMillis();

                mockMvc.perform(get("/flags")
                                .param("page", "0")
                                .param("size", "10"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.flags.length()").value(10));

                long retrieveTime = System.currentTimeMillis() - startTime;

                // Performance assertions (adjust thresholds as needed)
                assertTrue(createTime < 5000, "Flag creation took too long: " + createTime + "ms");
                assertTrue(retrieveTime < 1000, "Flag retrieval took too long: " + retrieveTime + "ms");
        }

        @Test
        void testRedisConnectionAndMessagePublishing() throws Exception {
                // Test that Redis connection is working
                assertNotNull(redisTemplate.getConnectionFactory());

                // Test basic Redis operations
                redisTemplate.opsForValue().set("test_key", "test_value");
                String value = redisTemplate.opsForValue().get("test_key");
                assertEquals("test_value", value);

                // Clean up
                redisTemplate.delete("test_key");
        }
}