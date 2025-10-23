package com.featureflags.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for paginated feature flag list response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlagListResponse {

    private List<FlagResponse> flags;
    private long total;
    private int page;
    private int size;
    private int totalPages;

    // Constructor with all fields (excluding totalPages which is calculated)
    public FlagListResponse(List<FlagResponse> flags, long total, int page, int size) {
        this.flags = flags;
        this.total = total;
        this.page = page;
        this.size = size;
        this.totalPages = (int) Math.ceil((double) total / size);
    }
}
