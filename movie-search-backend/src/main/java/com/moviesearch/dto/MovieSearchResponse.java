package com.moviesearch.dto;

import lombok.Builder;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
@Builder
public class MovieSearchResponse implements Serializable {
    private List<MovieResponse> movies;
    private int totalResults;
    private int currentPage;
    private int totalPages;
    private boolean hasNextPage;
    private boolean hasPreviousPage;
    private String searchTerm;
    private Long responseTimeMs;
}
