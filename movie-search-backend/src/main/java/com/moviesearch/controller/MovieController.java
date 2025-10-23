package com.moviesearch.controller;

import com.moviesearch.dto.MovieSearchRequest;
import com.moviesearch.dto.MovieSearchResponse;
import com.moviesearch.dto.MovieResponse;
import com.moviesearch.service.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/movies")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Movie Search", description = "Movie search and details API")
public class MovieController {

        private final MovieService movieService;

        /**
         * Search for movies
         */
        @GetMapping("/search")
        @Operation(summary = "Search movies", description = "Search for movies by title with optional filters")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved movies"),
                        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
                        @ApiResponse(responseCode = "503", description = "Service under maintenance")
        })
        public Mono<ResponseEntity<MovieSearchResponse>> searchMovies(
                        @Parameter(description = "Search term") @RequestParam String search,
                        @Parameter(description = "Page number") @RequestParam(defaultValue = "1") int page,
                        @Parameter(description = "Release year") @RequestParam(required = false) Integer year,
                        @Parameter(description = "Content type") @RequestParam(required = false) String type) {

                log.debug("Movie search request - search: {}, page: {}, year: {}, type: {}", search, page, year, type);

                MovieSearchRequest request = new MovieSearchRequest();
                request.setSearch(search);
                request.setPage(page);
                request.setYear(year);
                request.setType(type);

                return movieService.searchMovies(request)
                                .map(ResponseEntity::ok)
                                .doOnSuccess(response -> log.info("Movie search completed successfully"))
                                .doOnError(error -> log.error("Movie search failed: {}", error.getMessage()));
        }

        /**
         * Get movie details by IMDb ID
         */
        @GetMapping("/details/{imdbId}")
        @Operation(summary = "Get movie details", description = "Get detailed information about a specific movie")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved movie details"),
                        @ApiResponse(responseCode = "404", description = "Movie not found"),
                        @ApiResponse(responseCode = "503", description = "Service under maintenance")
        })
        public Mono<ResponseEntity<MovieResponse>> getMovieDetails(
                        @Parameter(description = "IMDb ID") @PathVariable String imdbId) {

                log.debug("Movie details request for IMDb ID: {}", imdbId);

                return movieService.getMovieDetails(imdbId)
                                .map(ResponseEntity::ok)
                                .doOnSuccess(response -> log.info("Movie details retrieved successfully for: {}",
                                                imdbId))
                                .doOnError(
                                                error -> log.error("Failed to retrieve movie details for {}: {}",
                                                                imdbId, error.getMessage()));
        }

        /**
         * Get specific feature flag status
         */
        @GetMapping("/flags/{flagName}")
        @Operation(summary = "Get feature flag", description = "Get status of a specific feature flag")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved feature flag status"),
                        @ApiResponse(responseCode = "404", description = "Feature flag not found")
        })
        public ResponseEntity<Map<String, Object>> getFeatureFlag(
                        @Parameter(description = "Feature flag name") @PathVariable String flagName) {
                log.debug("Feature flag request for: {}", flagName);

                try {
                        boolean enabled = movieService.isFeatureFlagEnabled(flagName);
                        return ResponseEntity.ok(Map.of(
                                        "name", flagName,
                                        "enabled", enabled));
                } catch (IllegalArgumentException e) {
                        return ResponseEntity.notFound().build();
                }
        }

}
