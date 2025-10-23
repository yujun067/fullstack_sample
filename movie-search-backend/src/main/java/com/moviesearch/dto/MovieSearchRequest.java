package com.moviesearch.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MovieSearchRequest {
    @NotBlank(message = "Search term cannot be empty")
    @Size(min = 2, max = 100, message = "Search term must be between 2 and 100 characters")
    private String search;

    @Min(value = 1, message = "Page must be at least 1")
    @Max(value = 100, message = "Page cannot exceed 100")
    private int page = 1;

    @Min(value = 1900, message = "Year must be at least 1900")
    @Max(value = 2030, message = "Year cannot exceed 2030")
    private Integer year;

    private String type; // movie, series, episode
}
