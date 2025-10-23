package com.moviesearch.client;

import com.moviesearch.dto.FeatureFlagBatchResponse;
import com.moviesearch.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "feature-flag-service", url = "${feature-flag.service.url:http://localhost:8080/feature}", configuration = FeignConfig.class)
public interface FeatureFlagClient {

    @PostMapping("/flags/batch")
    FeatureFlagBatchResponse getFeatureFlagsBatch(@RequestBody List<String> flagNames);
}
