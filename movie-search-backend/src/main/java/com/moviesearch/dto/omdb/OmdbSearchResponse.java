package com.moviesearch.dto.omdb;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * OMDb API search response DTO
 */
@Data
public class OmdbSearchResponse {

    @JsonProperty("Search")
    private List<OmdbMovie> search;

    @JsonProperty("totalResults")
    private String totalResults;

    @JsonProperty("Response")
    private String response;

    @JsonProperty("Error")
    private String error;

    /**
     * Check if the response is successful
     */
    public boolean isSuccessful() {
        return "True".equals(response) && error == null;
    }

    /**
     * Check if the response has search results
     */
    public boolean hasResults() {
        return isSuccessful() && search != null && !search.isEmpty();
    }
}
