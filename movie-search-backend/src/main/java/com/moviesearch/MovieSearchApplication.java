package com.moviesearch;

import com.moviesearch.service.FeatureFlagSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableFeignClients
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class MovieSearchApplication implements CommandLineRunner {

    private final FeatureFlagSyncService featureFlagSyncService;

    public static void main(String[] args) {
        SpringApplication.run(MovieSearchApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Initializing Movie Search Application...");

        // Initialize subscribed feature flags via Feign
        featureFlagSyncService.initializeSubscribedFlags();

        log.info("Movie Search Application started successfully!");
    }
}
