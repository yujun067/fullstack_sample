package com.moviesearch.service;

import com.moviesearch.client.OmdbApiClient;
import com.moviesearch.dto.MovieSearchRequest;
import com.moviesearch.dto.MovieSearchResponse;
import com.moviesearch.dto.MovieResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

        @Mock
        private OmdbApiClient omdbApiClient;

        @Mock
        private FeatureFlagConsumer featureFlagConsumer;

        @InjectMocks
        private MovieService movieService;

        private MovieSearchRequest searchRequest;
        private MovieSearchResponse searchResponse;
        private MovieResponse movieResponse;

        @BeforeEach
        void setUp() {
                searchRequest = new MovieSearchRequest();
                searchRequest.setSearch("batman");
                searchRequest.setPage(1);
                searchRequest.setYear(2008);
                searchRequest.setType("movie");

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
                when(omdbApiClient.searchMovies(any(MovieSearchRequest.class)))
                                .thenReturn(Mono.just(searchResponse));

                // When & Then
                StepVerifier.create(movieService.searchMovies(searchRequest))
                                .expectNext(searchResponse)
                                .verifyComplete();

                verify(omdbApiClient).searchMovies(searchRequest);
        }

        @Test
        void testSearchMovies_WithError() {
                // Given
                when(omdbApiClient.searchMovies(any(MovieSearchRequest.class)))
                                .thenReturn(Mono.error(new RuntimeException("API Error")));

                // When & Then
                StepVerifier.create(movieService.searchMovies(searchRequest))
                                .expectError(com.moviesearch.exception.ExternalApiException.class)
                                .verify();

                verify(omdbApiClient).searchMovies(searchRequest);
        }

        @Test
        void testGetMovieDetails_Success() {
                // Given
                when(omdbApiClient.getMovieDetails("tt0372784"))
                                .thenReturn(Mono.just(movieResponse));

                // When & Then
                StepVerifier.create(movieService.getMovieDetails("tt0372784"))
                                .expectNext(movieResponse)
                                .verifyComplete();

                verify(omdbApiClient).getMovieDetails("tt0372784");
        }

        @Test
        void testGetMovieDetails_WithError() {
                // Given
                when(omdbApiClient.getMovieDetails("tt0372784"))
                                .thenReturn(Mono.error(new RuntimeException("API Error")));

                // When & Then
                StepVerifier.create(movieService.getMovieDetails("tt0372784"))
                                .expectError(com.moviesearch.exception.ExternalApiException.class)
                                .verify();

                verify(omdbApiClient).getMovieDetails("tt0372784");
        }

        @Test
        void testIsFeatureFlagEnabled_Success() {
                // Given
                String flagName = "test_flag";
                when(featureFlagConsumer.getFeatureFlag(flagName))
                                .thenReturn(true);

                // When
                boolean result = movieService.isFeatureFlagEnabled(flagName);

                // Then
                assertTrue(result);
                verify(featureFlagConsumer).getFeatureFlag(flagName);
        }

        @Test
        void testIsFeatureFlagEnabled_Disabled() {
                // Given
                String flagName = "test_flag";
                when(featureFlagConsumer.getFeatureFlag(flagName))
                                .thenReturn(false);

                // When
                boolean result = movieService.isFeatureFlagEnabled(flagName);

                // Then
                assertFalse(result);
                verify(featureFlagConsumer).getFeatureFlag(flagName);
        }

        @Test
        void testIsFeatureFlagEnabled_FlagNotFound() {
                // Given
                String flagName = "nonexistent_flag";
                when(featureFlagConsumer.getFeatureFlag(flagName))
                                .thenReturn(null);

                // When & Then
                IllegalArgumentException exception = assertThrows(
                                IllegalArgumentException.class,
                                () -> movieService.isFeatureFlagEnabled(flagName));

                assertEquals("Feature flag not found: " + flagName, exception.getMessage());
                verify(featureFlagConsumer).getFeatureFlag(flagName);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = { "   ", "\t", "\n" })
        void testIsFeatureFlagEnabled_InvalidFlagName(String flagName) {
                // When & Then
                IllegalArgumentException exception = assertThrows(
                                IllegalArgumentException.class,
                                () -> movieService.isFeatureFlagEnabled(flagName));

                assertEquals("Feature flag name cannot be null or empty", exception.getMessage());
        }

}
