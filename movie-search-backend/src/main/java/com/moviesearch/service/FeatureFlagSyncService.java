package com.moviesearch.service;

import com.moviesearch.dto.FeatureFlagBatchResponse;
import com.moviesearch.client.FeatureFlagClient;
import com.moviesearch.config.FeatureFlagConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeatureFlagSyncService {

    private final FeatureFlagClient featureFlagClient;
    private final FeatureFlagConfig featureFlagConfig;
    private final FeatureFlagConsumer featureFlagConsumer;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeSubscribedFlags() {
        log.info("Initializing subscribed feature flags via Feign batch fetch...");
        refreshSubscribedFlags();
    }

    @Scheduled(fixedRateString = "${feature-flag.refresh-interval:300000}")
    public void refreshSubscribedFlags() {
        Map<String, Boolean> subscribedFlags = featureFlagConfig.getSubscribed();
        if (subscribedFlags == null || subscribedFlags.isEmpty()) {
            log.debug("No subscribed feature flags configured; skipping refresh");
            return;
        }

        List<String> subscribedFlagNames = List.copyOf(subscribedFlags.keySet());
        try {
            FeatureFlagBatchResponse response = featureFlagClient.getFeatureFlagsBatch(subscribedFlagNames);
            if (response == null || response.getFlags() == null || response.getFlags().isEmpty()) {
                log.warn("Batch fetch returned empty for subscribed flags: {}", subscribedFlagNames);
                return;
            }

            // Update only subscribed flags into local cache
            response.getFlags().forEach((name, flagInfo) -> {
                if (flagInfo.getEnabled() != null) {
                    featureFlagConsumer.updateFeatureFlag(name, flagInfo.getEnabled());
                }
            });
            log.info("Refreshed subscribed feature flags: {}", response.getFlags().keySet());
        } catch (Exception e) {
            log.error("Error refreshing subscribed feature flags via Feign: {}", e.getMessage());
        }
    }
}
