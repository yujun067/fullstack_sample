package com.moviesearch.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moviesearch.dto.FeatureFlagEventDTO;
import com.moviesearch.service.FeatureFlagConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Component;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class RedisMessageListenerConfig {

    private final FeatureFlagConsumer featureFlagConsumer;
    private final ObjectMapper objectMapper;

    @Bean
    public ChannelTopic featureFlagTopic() {
        return new ChannelTopic("feature-flag-events");
    }

    @Bean
    public MessageListenerAdapter featureFlagMessageListener() {
        MessageListenerAdapter adapter = new MessageListenerAdapter(
                new FeatureFlagMessageListener(featureFlagConsumer, objectMapper),
                "handleMessage");
        // Use StringRedisSerializer to receive string messages from Redis pub/sub
        adapter.setSerializer(new org.springframework.data.redis.serializer.StringRedisSerializer());
        return adapter;
    }

    @Bean
    public RedisMessageListenerContainer redisContainer(RedisConnectionFactory connectionFactory) {
        log.info("[REDIS] Initializing Redis message listener container...");
        log.info("[REDIS] Connection factory: {}", connectionFactory.getClass().getSimpleName());

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(featureFlagMessageListener(), featureFlagTopic());

        log.info("[REDIS] Added message listener for topic: {}", featureFlagTopic().getTopic());
        log.info("[REDIS] Message listener: {}", featureFlagMessageListener().getClass().getSimpleName());

        return container;
    }

    /**
     * Message listener for feature flag events using structured JSON format.
     */
    @Component
    public static class FeatureFlagMessageListener {
        private final FeatureFlagConsumer featureFlagConsumer;
        private final ObjectMapper objectMapper;

        public FeatureFlagMessageListener(FeatureFlagConsumer featureFlagConsumer, ObjectMapper objectMapper) {
            this.featureFlagConsumer = featureFlagConsumer;
            this.objectMapper = objectMapper;
        }

        /**
         * Handle incoming feature flag event messages.
         * Expects JSON format with structured event data.
         */
        public void handleMessage(String message) {
            log.info("[PUB/SUB] Received Redis message: {}", message);

            try {
                log.info("[PUB/SUB] Starting to parse JSON message...");
                // Parse JSON message to structured event
                FeatureFlagEventDTO event = objectMapper.readValue(message, FeatureFlagEventDTO.class);
                log.info(
                        "[PUB/SUB] Successfully parsed event: flagName={}, eventType={}, enabled={}, timestamp={}, messageId={}",
                        event.getFlagName(), event.getEventType(), event.getEnabled(), event.getTimestamp(),
                        event.getMessageId());

                log.info("[PUB/SUB] Starting to process event...");
                processEvent(event);
                log.info("[PUB/SUB] Successfully processed event for flag: {}", event.getFlagName());
            } catch (Exception e) {
                log.error("[PUB/SUB] Error processing feature flag event message: {}", e.getMessage(), e);
                // Consider implementing dead letter queue for failed messages
                handleFailedMessage(message, e);
            }
        }

        /**
         * Process structured feature flag event.
         */
        private void processEvent(FeatureFlagEventDTO event) {
            log.info("[PUB/SUB] Processing feature flag event: {} for flag: {} at {}",
                    event.getEventType(), event.getFlagName(), event.getTimestamp());

            switch (event.getEventType()) {
                case CREATED:
                case UPDATED:
                    if (event.getEnabled() != null) {
                        log.info("[PUB/SUB] Updating feature flag '{}' to {} with messageId {}",
                                event.getFlagName(), event.getEnabled(), event.getMessageId());
                        // Use messageId for deduplication, accept eventual consistency
                        featureFlagConsumer.updateFeatureFlag(event.getFlagName(), event.getEnabled(),
                                event.getMessageId());
                        log.info("[PUB/SUB] Successfully updated feature flag '{}' to {}",
                                event.getFlagName(), event.getEnabled());
                    } else {
                        log.warn("[PUB/SUB] Event enabled value is null for flag: {}", event.getFlagName());
                    }
                    break;
                case DELETED:
                    log.info("[PUB/SUB] Removing feature flag '{}'", event.getFlagName());
                    featureFlagConsumer.removeFeatureFlag(event.getFlagName());
                    log.info("[PUB/SUB] Removed feature flag '{}'", event.getFlagName());
                    break;
                default:
                    log.warn("[PUB/SUB] Unknown event type: {} for flag: {}",
                            event.getEventType(), event.getFlagName());
            }
        }

        /**
         * Handle failed message processing.
         * In production, this could send to a dead letter queue.
         */
        private void handleFailedMessage(String message, Exception e) {
            // TODO: Implement alerting mechanism
            // For now, just log the error
            log.error("Failed to process message, storing for manual review: {}", message);
        }
    }

}
