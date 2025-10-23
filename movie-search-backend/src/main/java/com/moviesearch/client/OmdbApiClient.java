package com.moviesearch.client;

import com.moviesearch.dto.MovieSearchRequest;
import com.moviesearch.dto.MovieSearchResponse;
import com.moviesearch.dto.MovieResponse;
import com.moviesearch.dto.omdb.OmdbSearchResponse;
import com.moviesearch.dto.omdb.OmdbMovieDetailsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.net.ConnectException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OmdbApiClient {

        private final WebClient webClient;

        @Value("${omdb.api.key}")
        private String apiKey;

        // GET https://www.omdbapi.com/?apikey=xxx&s=keyword&page=1&y=2023&type=movie
        @Cacheable(value = "movieSearch", key = "#request.search + '_' + #request.page + '_' + (#request.year ?: 'null') + '_' + (#request.type ?:'null')")
        public Mono<MovieSearchResponse> searchMovies(MovieSearchRequest request) {
                log.debug("Searching movies with request: {}", request);

                return webClient
                                .get()
                                .uri(uriBuilder -> uriBuilder
                                                .path("/")
                                                .queryParam("apikey", apiKey)
                                                .queryParam("s", request.getSearch())
                                                .queryParam("page", request.getPage())
                                                .queryParamIfPresent("y", Optional.ofNullable(request.getYear()))
                                                .queryParamIfPresent("type", Optional.ofNullable(request.getType()))
                                                .build())
                                .retrieve()
                                .bodyToMono(OmdbSearchResponse.class)
                                .retryWhen(createOptimizedRetryStrategy("movie search"))
                                .map(response -> mapToMovieSearchResponse(response, request))
                                .doOnSuccess(response -> log.debug("Successfully retrieved {} movies",
                                                response.getMovies().size()))
                                .doOnError(error -> log.error("Error searching movies: {}", error.getMessage()));
        }

        // GET https://www.omdbapi.com/?apikey=xxx&i=tt0133093&plot=full
        @Cacheable(value = "movieDetails", key = "#imdbId")
        public Mono<MovieResponse> getMovieDetails(String imdbId) {
                log.debug("Fetching movie details for IMDb ID: {}", imdbId);

                return webClient
                                .get()
                                .uri(uriBuilder -> uriBuilder
                                                .path("/")
                                                .queryParam("apikey", apiKey)
                                                .queryParam("i", imdbId)
                                                .queryParam("plot", "full")
                                                .build())
                                .retrieve()
                                .bodyToMono(OmdbMovieDetailsResponse.class)
                                .retryWhen(createOptimizedRetryStrategy("movie details"))
                                .map(this::mapToMovieResponse)
                                .doOnSuccess(response -> log.debug("Successfully retrieved movie details for: {}",
                                                response.getTitle()))
                                .doOnError(error -> log.error("Error fetching movie details: {}", error.getMessage()));
        }

        /**
         * Create optimized retry strategy for OMDB API calls
         */
        private Retry createOptimizedRetryStrategy(String operation) {
                return Retry.backoff(3, Duration.ofSeconds(1))
                                .filter(throwable -> {
                                        // Only retry on server errors (5xx) and network issues
                                        if (throwable instanceof WebClientResponseException ex) {
                                                return ex.getStatusCode().is5xxServerError();
                                        }
                                        // Retry on network connectivity issues
                                        return throwable instanceof ConnectException ||
                                                        throwable instanceof TimeoutException ||
                                                        throwable instanceof java.net.SocketTimeoutException;
                                })
                                .doBeforeRetry(retrySignal -> log.warn("Retrying {} API call, attempt: {}, error: {}",
                                                operation, retrySignal.totalRetries() + 1,
                                                retrySignal.failure().getMessage()));
        }

        private MovieSearchResponse mapToMovieSearchResponse(OmdbSearchResponse response, MovieSearchRequest request) {
                if (!response.hasResults()) {
                        return MovieSearchResponse.builder()
                                        .movies(List.of())
                                        .totalResults(0)
                                        .currentPage(request.getPage())
                                        .totalPages(0)
                                        .hasNextPage(false)
                                        .hasPreviousPage(false)
                                        .searchTerm("")
                                        .responseTimeMs(0L)
                                        .build();
                }

                // Convert search results to MovieResponse objects
                List<MovieResponse> movies = response.getSearch().stream()
                                .map(this::mapOmdbMovieToMovieResponse)
                                .collect(java.util.stream.Collectors.toList());

                log.debug("Processed {} search results for page {}", movies.size(), request.getPage());

                String totalResultsStr = response.getTotalResults();
                int totalResults = totalResultsStr != null ? Integer.parseInt(totalResultsStr) : 0;
                int currentPage = request.getPage();
                int totalPages = (int) Math.ceil((double) totalResults / 10); // OMDb returns 10 results per page

                return MovieSearchResponse.builder()
                                .movies(movies)
                                .totalResults(totalResults)
                                .currentPage(currentPage)
                                .totalPages(totalPages)
                                .hasNextPage(currentPage < totalPages)
                                .hasPreviousPage(currentPage > 1)
                                .searchTerm("")
                                .responseTimeMs(System.currentTimeMillis())
                                .build();
        }

        private MovieResponse mapToMovieResponse(OmdbMovieDetailsResponse response) {
                return MovieResponse.builder()
                                .imdbId(response.getImdbId())
                                .title(response.getTitle())
                                .year(response.getYear())
                                .rated(response.getRated())
                                .released(response.getReleased())
                                .runtime(response.getRuntime())
                                .genre(response.getGenre())
                                .director(response.getDirector())
                                .writer(response.getWriter())
                                .actors(response.getActors())
                                .plot(response.getPlot())
                                .language(response.getLanguage())
                                .country(response.getCountry())
                                .awards(response.getAwards())
                                .poster(response.getPoster())
                                .imdbRating(response.getImdbRating())
                                .imdbVotes(response.getImdbVotes())
                                .type(response.getType())
                                .dvd(response.getDvd())
                                .boxOffice(response.getBoxOffice())
                                .production(response.getProduction())
                                .website(response.getWebsite())
                                .cachedAt(java.time.LocalDateTime.now())
                                .build();
        }

        private MovieResponse mapOmdbMovieToMovieResponse(com.moviesearch.dto.omdb.OmdbMovie omdbMovie) {
                return MovieResponse.builder()
                                .imdbId(omdbMovie.getImdbId())
                                .title(omdbMovie.getTitle())
                                .year(omdbMovie.getYear())
                                .rated(omdbMovie.getRated())
                                .released(omdbMovie.getReleased())
                                .runtime(omdbMovie.getRuntime())
                                .genre(omdbMovie.getGenre())
                                .director(omdbMovie.getDirector())
                                .writer(omdbMovie.getWriter())
                                .actors(omdbMovie.getActors())
                                .plot(omdbMovie.getPlot())
                                .language(omdbMovie.getLanguage())
                                .country(omdbMovie.getCountry())
                                .awards(omdbMovie.getAwards())
                                .poster(omdbMovie.getPoster())
                                .imdbRating(omdbMovie.getImdbRating())
                                .imdbVotes(omdbMovie.getImdbVotes())
                                .type(omdbMovie.getType())
                                .dvd(omdbMovie.getDvd())
                                .boxOffice(omdbMovie.getBoxOffice())
                                .production(omdbMovie.getProduction())
                                .website(omdbMovie.getWebsite())
                                .cachedAt(java.time.LocalDateTime.now())
                                .build();
        }
}
