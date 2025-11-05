package com.moviesearch.service;

import com.moviesearch.client.OmdbApiClient;
import com.moviesearch.dto.MovieSearchRequest;
import com.moviesearch.dto.MovieSearchResponse;
import com.moviesearch.dto.MovieResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieService {

    private final OmdbApiClient omdbApiClient;
    private final FeatureFlagConsumer featureFlagConsumer;

    /**
     * Search for movies with feature flag integration
     */
    public Mono<MovieSearchResponse> searchMovies(MovieSearchRequest request) {
        log.debug("Searching movies with request: {}", request);

        long startTime = System.currentTimeMillis();

        return omdbApiClient.searchMovies(request)
                .map(response -> {
                    long responseTime = System.currentTimeMillis() - startTime;
                    response.setResponseTimeMs(responseTime);
                    response.setSearchTerm(request.getSearch());

                    log.info("Movie search completed in {}ms for query: '{}', found {} results",
                            responseTime, request.getSearch(), response.getTotalResults());

                    return response;
                })
                .doOnError(error -> log.error("Error searching movies: {}", error.getMessage()))
                .timeout(Duration.ofSeconds(15))
                .onErrorMap(throwable -> {
                    log.error("Movie search failed: {}", throwable.getMessage());
                    return new com.moviesearch.exception.ExternalApiException(
                            "Movie search service is temporarily unavailable. Please try again later.", throwable);
                });
    }

    /**
     * Get movie details by IMDb ID
     */
    public Mono<MovieResponse> getMovieDetails(String imdbId) {
        log.debug("Fetching movie details for IMDb ID: {}", imdbId);

        return omdbApiClient.getMovieDetails(imdbId)
                .doOnSuccess(response -> log.info("Successfully retrieved movie details for: {}", response.getTitle()))
                .doOnError(error -> log.error("Error fetching movie details: {}", error.getMessage()))
                .timeout(Duration.ofSeconds(15))
                .onErrorMap(throwable -> {
                    log.error("Movie details fetch failed: {}", throwable.getMessage());
                    return new com.moviesearch.exception.ExternalApiException(
                            "Movie details service is temporarily unavailable. Please try again later.", throwable);
                });
    }

    /**
     * Check if a specific feature flag is enabled
     */
    public boolean isFeatureFlagEnabled(String flagName) {
        if (flagName == null || flagName.trim().isEmpty()) {
            throw new IllegalArgumentException("Feature flag name cannot be null or empty");
        }

        Boolean enabled = featureFlagConsumer.getFeatureFlag(flagName);
        if (enabled == null) {
            throw new IllegalArgumentException("Feature flag not found: " + flagName);
        }

        return enabled;
    }

}
