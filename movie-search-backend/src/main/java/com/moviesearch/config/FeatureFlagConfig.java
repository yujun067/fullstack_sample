package com.moviesearch.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "feature-flag")
@Data
public class FeatureFlagConfig {

    private Service service = new Service();
    private Map<String, Boolean> subscribed;
    private long refreshInterval = 300000; // 5 minutes default

    @Data
    public static class Service {
        private String url;
    }
}
