package com.moviesearch.dto;

import lombok.Builder;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
public class MovieResponse implements Serializable {
    private String imdbId;
    private String title;
    private String year;
    private String rated;
    private String released;
    private String runtime;
    private String genre;
    private String director;
    private String writer;
    private String actors;
    private String plot;
    private String language;
    private String country;
    private String awards;
    private String poster;
    private String imdbRating;
    private String imdbVotes;
    private String type;
    private String dvd;
    private String boxOffice;
    private String production;
    private String website;
    private LocalDateTime cachedAt;
}
