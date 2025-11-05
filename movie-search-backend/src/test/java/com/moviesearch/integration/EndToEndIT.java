package com.moviesearch.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moviesearch.client.OmdbApiClient;
import com.moviesearch.config.BaseIntegrationTest;
import com.moviesearch.dto.FeatureFlagEventDTO;
import com.moviesearch.dto.MovieResponse;
import com.moviesearch.dto.MovieSearchRequest;
import com.moviesearch.dto.MovieSearchResponse;
import com.moviesearch.service.FeatureFlagConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * End-to-End integration tests for the Movie Search application.
 * Tests the complete workflow including API endpoints, feature flag
 * integration,
 * and Redis pub/sub messaging.
 * 
 * This test verifies:
 * 1. Movie search API endpoint
 * 2. Movie details API endpoint
 * 3. Feature flag API endpoint
 * 4. Feature flag integration (maintenance mode)
 * 5. Redis pub/sub message handling
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class EndToEndIT extends BaseIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private FeatureFlagConsumer featureFlagConsumer;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OmdbApiClient omdbApiClient;

    private MovieSearchResponse mockSearchResponse;
    private MovieResponse mockMovieResponse;

    @BeforeEach
    void setUp() {
        // Setup mock movie responses
        MovieResponse movie1 = MovieResponse.builder()
                .imdbId("tt0372784")
                .title("The Dark Knight")
                .year("2008")
                .type("movie")
                .poster("https://example.com/poster.jpg")
                .plot("When the menace known as the Joker wreaks havoc...")
                .genre("Action, Crime, Drama")
                .director("Christopher Nolan")
                .actors("Christian Bale, Heath Ledger, Aaron Eckhart")
                .imdbRating("9.0")
                .build();

        mockSearchResponse = MovieSearchResponse.builder()
                .movies(Arrays.asList(movie1))
                .totalResults(1)
                .currentPage(1)
                .totalPages(1)
                .hasNextPage(false)
                .hasPreviousPage(false)
                .searchTerm("batman")
                .responseTimeMs(500L)
                .build();

        mockMovieResponse = movie1;

        // Mock OMDB API client
        when(omdbApiClient.searchMovies(any(MovieSearchRequest.class)))
                .thenReturn(Mono.just(mockSearchResponse));

        when(omdbApiClient.getMovieDetails(any(String.class)))
                .thenReturn(Mono.just(mockMovieResponse));

        // Initialize feature flags
        featureFlagConsumer.updateFeatureFlag("maintenance_mode", false);
    }

    @Test
    void testSearchMovies_Success() {
        // When & Then
        webTestClient.get()
                .uri("/movies/search?search=batman&page=1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(MovieSearchResponse.class)
                .value(response -> {
                    assertNotNull(response);
                    assertEquals(1, response.getTotalResults());
                    assertEquals("batman", response.getSearchTerm());
                    assertEquals(1, response.getCurrentPage());
                    assertNotNull(response.getMovies());
                    assertFalse(response.getMovies().isEmpty());
                    assertEquals("The Dark Knight", response.getMovies().get(0).getTitle());
                });
    }

    @Test
    void testSearchMovies_WithFilters() {
        // When & Then
        webTestClient.get()
                .uri("/movies/search?search=batman&page=1&year=2008&type=movie")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(MovieSearchResponse.class)
                .value(response -> {
                    assertNotNull(response);
                    assertEquals(1, response.getTotalResults());
                    assertNotNull(response.getMovies());
                });
    }

    @Test
    void testGetMovieDetails_Success() {
        // When & Then
        webTestClient.get()
                .uri("/movies/details/tt0372784")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(MovieResponse.class)
                .value(response -> {
                    assertNotNull(response);
                    assertEquals("tt0372784", response.getImdbId());
                    assertEquals("The Dark Knight", response.getTitle());
                    assertEquals("2008", response.getYear());
                    assertEquals("Christopher Nolan", response.getDirector());
                });
    }

    @Test
    void testGetFeatureFlag_Success() {
        // Given
        featureFlagConsumer.updateFeatureFlag("test_flag", true);

        // When & Then
        webTestClient.get()
                .uri("/movies/flags/test_flag")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.name").isEqualTo("test_flag")
                .jsonPath("$.enabled").isEqualTo(true);
    }

    @Test
    void testGetFeatureFlag_NotFound() {
        // When & Then
        webTestClient.get()
                .uri("/movies/flags/nonexistent_flag")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testFeatureFlagIntegration_MaintenanceMode() {
        // Given - Enable maintenance mode
        featureFlagConsumer.updateFeatureFlag("maintenance_mode", true);

        // When - Try to search movies
        // Maintenance mode should return 503 Service Unavailable
        webTestClient.get()
                .uri("/movies/search?search=batman&page=1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE); // Maintenance mode should return 503

        // Cleanup
        featureFlagConsumer.updateFeatureFlag("maintenance_mode", false);
    }

    @Test
    void testRedisPubSub_FeatureFlagUpdate() throws Exception {
        // Verify Redis connection is available
        assertNotNull(redisTemplate.getConnectionFactory());

        // Given - Initial flag state
        featureFlagConsumer.updateFeatureFlag("e2e_test_flag", false);
        Boolean initialValue = featureFlagConsumer.getFeatureFlag("e2e_test_flag");
        assertNotNull(initialValue);
        assertFalse(initialValue);

        // When - Publish feature flag update event via Redis pub/sub
        FeatureFlagEventDTO event = FeatureFlagEventDTO.builder()
                .eventType(FeatureFlagEventDTO.EventType.UPDATED)
                .flagName("e2e_test_flag")
                .enabled(true)
                .timestamp(LocalDateTime.now())
                .messageId(UUID.randomUUID().toString())
                .version("1.0")
                .build();

        String message = objectMapper.writeValueAsString(event);
        // Use StringRedisTemplate for pub/sub to match the listener's expected format
        org.springframework.data.redis.core.StringRedisTemplate stringRedisTemplate = new org.springframework.data.redis.core.StringRedisTemplate(
                redisTemplate.getConnectionFactory());
        stringRedisTemplate.convertAndSend("feature-flag-events", message);

        // Wait for message processing
        Thread.sleep(1000);

        // Then - Verify flag was updated
        Boolean updatedValue = featureFlagConsumer.getFeatureFlag("e2e_test_flag");
        assertNotNull(updatedValue);
        assertTrue(updatedValue, "Feature flag should be updated via Redis pub/sub");

        // Cleanup
        featureFlagConsumer.removeFeatureFlag("e2e_test_flag");
    }

    @Test
    void testRedisPubSub_FeatureFlagDelete() throws Exception {
        // Verify Redis connection is available
        assertNotNull(redisTemplate.getConnectionFactory());

        // Given - Create a flag
        featureFlagConsumer.updateFeatureFlag("e2e_delete_flag", true);
        assertNotNull(featureFlagConsumer.getFeatureFlag("e2e_delete_flag"));

        // When - Publish feature flag delete event via Redis pub/sub
        FeatureFlagEventDTO event = FeatureFlagEventDTO.builder()
                .eventType(FeatureFlagEventDTO.EventType.DELETED)
                .flagName("e2e_delete_flag")
                .timestamp(LocalDateTime.now())
                .messageId(UUID.randomUUID().toString())
                .version("1.0")
                .build();

        String message = objectMapper.writeValueAsString(event);
        // Use StringRedisTemplate for pub/sub to match the listener's expected format
        org.springframework.data.redis.core.StringRedisTemplate stringRedisTemplate = new org.springframework.data.redis.core.StringRedisTemplate(
                redisTemplate.getConnectionFactory());
        stringRedisTemplate.convertAndSend("feature-flag-events", message);

        // Wait for message processing
        Thread.sleep(1000);

        // Then - Verify flag was deleted
        Boolean deletedFlag = featureFlagConsumer.getFeatureFlag("e2e_delete_flag");
        assertNull(deletedFlag, "Feature flag should be deleted via Redis pub/sub");
    }

    @Test
    void testRedisConnection() {
        // Test that Redis connection is working
        assertNotNull(redisTemplate.getConnectionFactory());

        // Test basic Redis operations
        redisTemplate.opsForValue().set("test_key", "test_value");
        Object value = redisTemplate.opsForValue().get("test_key");
        assertEquals("test_value", value);

        // Clean up
        redisTemplate.delete("test_key");
    }

    @Test
    void testCompleteWorkflow() {
        // 1. Search movies
        webTestClient.get()
                .uri("/movies/search?search=batman&page=1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(MovieSearchResponse.class)
                .value(response -> {
                    assertNotNull(response);
                    assertFalse(response.getMovies().isEmpty());
                    String imdbId = response.getMovies().get(0).getImdbId();

                    // 2. Get movie details using IMDb ID from search results
                    webTestClient.get()
                            .uri("/movies/details/" + imdbId)
                            .accept(MediaType.APPLICATION_JSON)
                            .exchange()
                            .expectStatus().isOk()
                            .expectBody(MovieResponse.class)
                            .value(movie -> {
                                assertNotNull(movie);
                                assertEquals(imdbId, movie.getImdbId());
                            });
                });

        // 3. Check feature flag
        featureFlagConsumer.updateFeatureFlag("workflow_flag", true);
        webTestClient.get()
                .uri("/movies/flags/workflow_flag")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.enabled").isEqualTo(true);

        // Cleanup
        featureFlagConsumer.removeFeatureFlag("workflow_flag");
    }
}
