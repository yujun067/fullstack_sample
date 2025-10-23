package com.moviesearch.service;

import com.moviesearch.client.OmdbApiClient;
import com.moviesearch.dto.MovieSearchRequest;
import com.moviesearch.dto.MovieSearchResponse;
import com.moviesearch.dto.MovieResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;

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

}
