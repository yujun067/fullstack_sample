package com.moviesearch.controller;

import com.moviesearch.dto.MovieSearchRequest;
import com.moviesearch.dto.MovieSearchResponse;
import com.moviesearch.dto.MovieResponse;
import com.moviesearch.service.MovieService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MovieController.
 * Tests all API endpoints and error handling scenarios.
 */
@ExtendWith(MockitoExtension.class)
class MovieControllerTest {

    @Mock
    private MovieService movieService;

    @InjectMocks
    private MovieController movieController;

    private MovieSearchRequest searchRequest;
    private MovieSearchResponse searchResponse;
    private MovieResponse movieResponse;

    @BeforeEach
    void setUp() {
        // Setup test data
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

        searchResponse = MovieSearchResponse.builder()
                .movies(Arrays.asList(movie1))
                .totalResults(1)
                .currentPage(1)
                .totalPages(1)
                .hasNextPage(false)
                .hasPreviousPage(false)
                .searchTerm("batman")
                .responseTimeMs(500L)
                .build();

        movieResponse = movie1;
    }

    @Test
    void testSearchMovies_Success() {
        // Given
        when(movieService.searchMovies(any(MovieSearchRequest.class)))
                .thenReturn(Mono.just(searchResponse));

        // When
        Mono<org.springframework.http.ResponseEntity<MovieSearchResponse>> result = movieController
                .searchMovies("batman", 1, 2008, "movie");

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(200, response.getStatusCode().value());
                    assertNotNull(response.getBody());
                    assertEquals(1, response.getBody().getTotalResults());
                    assertEquals("batman", response.getBody().getSearchTerm());
                    assertEquals(1, response.getBody().getCurrentPage());
                })
                .verifyComplete();

        verify(movieService).searchMovies(any(MovieSearchRequest.class));
    }

    @Test
    void testSearchMovies_WithDefaultParameters() {
        // Given
        when(movieService.searchMovies(any(MovieSearchRequest.class)))
                .thenReturn(Mono.just(searchResponse));

        // When
        Mono<org.springframework.http.ResponseEntity<MovieSearchResponse>> result = movieController
                .searchMovies("batman", 1, null, null);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(200, response.getStatusCode().value());
                    assertNotNull(response.getBody());
                })
                .verifyComplete();

        verify(movieService).searchMovies(any(MovieSearchRequest.class));
    }

    @Test
    void testSearchMovies_WithError() {
        // Given
        when(movieService.searchMovies(any(MovieSearchRequest.class)))
                .thenReturn(Mono.error(new RuntimeException("API Error")));

        // When
        Mono<org.springframework.http.ResponseEntity<MovieSearchResponse>> result = movieController
                .searchMovies("batman", 1, null, null);

        // Then
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(movieService).searchMovies(any(MovieSearchRequest.class));
    }

    @Test
    void testGetMovieDetails_Success() {
        // Given
        when(movieService.getMovieDetails("tt0372784"))
                .thenReturn(Mono.just(movieResponse));

        // When
        Mono<org.springframework.http.ResponseEntity<MovieResponse>> result = movieController
                .getMovieDetails("tt0372784");

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(200, response.getStatusCode().value());
                    assertNotNull(response.getBody());
                    assertEquals("tt0372784", response.getBody().getImdbId());
                    assertEquals("The Dark Knight", response.getBody().getTitle());
                    assertEquals("2008", response.getBody().getYear());
                })
                .verifyComplete();

        verify(movieService).getMovieDetails("tt0372784");
    }

    @Test
    void testGetMovieDetails_WithError() {
        // Given
        when(movieService.getMovieDetails("tt9999999"))
                .thenReturn(Mono.error(new RuntimeException("Movie not found")));

        // When
        Mono<org.springframework.http.ResponseEntity<MovieResponse>> result = movieController
                .getMovieDetails("tt9999999");

        // Then
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(movieService).getMovieDetails("tt9999999");
    }

    @Test
    void testGetFeatureFlag_Success() {
        // Given
        when(movieService.isFeatureFlagEnabled("dark_mode"))
                .thenReturn(true);

        // When
        org.springframework.http.ResponseEntity<java.util.Map<String, Object>> result = movieController
                .getFeatureFlag("dark_mode");

        // Then
        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertEquals("dark_mode", result.getBody().get("name"));
        assertEquals(true, result.getBody().get("enabled"));

        verify(movieService).isFeatureFlagEnabled("dark_mode");
    }

    @Test
    void testGetFeatureFlag_NotFound() {
        // Given
        when(movieService.isFeatureFlagEnabled("nonexistent_flag"))
                .thenThrow(new IllegalArgumentException("Feature flag not found"));

        // When
        org.springframework.http.ResponseEntity<java.util.Map<String, Object>> result = movieController
                .getFeatureFlag("nonexistent_flag");

        // Then
        assertEquals(404, result.getStatusCode().value());
        assertNull(result.getBody());

        verify(movieService).isFeatureFlagEnabled("nonexistent_flag");
    }

    @Test
    void testGetFeatureFlag_NullFlagName() {
        // Given
        when(movieService.isFeatureFlagEnabled(null))
                .thenThrow(new IllegalArgumentException("Feature flag name cannot be null or empty"));

        // When
        org.springframework.http.ResponseEntity<java.util.Map<String, Object>> result = movieController
                .getFeatureFlag(null);

        // Then
        assertEquals(404, result.getStatusCode().value());
        assertNull(result.getBody());

        verify(movieService).isFeatureFlagEnabled(null);
    }

    @Test
    void testGetFeatureFlag_EmptyFlagName() {
        // Given
        when(movieService.isFeatureFlagEnabled(""))
                .thenThrow(new IllegalArgumentException("Feature flag name cannot be null or empty"));

        // When
        org.springframework.http.ResponseEntity<java.util.Map<String, Object>> result = movieController
                .getFeatureFlag("");

        // Then
        assertEquals(404, result.getStatusCode().value());
        assertNull(result.getBody());

        verify(movieService).isFeatureFlagEnabled("");
    }

    @Test
    void testSearchMovies_RequestMapping() {
        // Given
        when(movieService.searchMovies(any(MovieSearchRequest.class)))
                .thenReturn(Mono.just(searchResponse));

        // When - Test with different parameter combinations
        Mono<org.springframework.http.ResponseEntity<MovieSearchResponse>> result1 = movieController
                .searchMovies("action", 2, 2020, "movie");

        Mono<org.springframework.http.ResponseEntity<MovieSearchResponse>> result2 = movieController
                .searchMovies("drama", 1, null, "series");

        // Then
        StepVerifier.create(result1)
                .assertNext(response -> assertEquals(200, response.getStatusCode().value()))
                .verifyComplete();

        StepVerifier.create(result2)
                .assertNext(response -> assertEquals(200, response.getStatusCode().value()))
                .verifyComplete();

        verify(movieService, times(2)).searchMovies(any(MovieSearchRequest.class));
    }

    @Test
    void testGetMovieDetails_RequestMapping() {
        // Given
        when(movieService.getMovieDetails(anyString()))
                .thenReturn(Mono.just(movieResponse));

        // When - Test with different IMDb IDs
        Mono<org.springframework.http.ResponseEntity<MovieResponse>> result1 = movieController
                .getMovieDetails("tt0372784");

        Mono<org.springframework.http.ResponseEntity<MovieResponse>> result2 = movieController
                .getMovieDetails("tt0468569");

        // Then
        StepVerifier.create(result1)
                .assertNext(response -> assertEquals(200, response.getStatusCode().value()))
                .verifyComplete();

        StepVerifier.create(result2)
                .assertNext(response -> assertEquals(200, response.getStatusCode().value()))
                .verifyComplete();

        verify(movieService, times(2)).getMovieDetails(anyString());
    }
}
