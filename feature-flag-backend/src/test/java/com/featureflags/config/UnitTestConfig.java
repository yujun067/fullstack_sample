package com.featureflags.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.mockito.Mockito.mock;

/**
 * Configuration for unit tests that mocks external dependencies.
 * This avoids the need for real Redis/MySQL connections in unit tests.
 */
@TestConfiguration
public class UnitTestConfig {

    @Bean
    @Primary
    @SuppressWarnings("unchecked")
    public RedisTemplate<String, Object> mockRedisTemplate() {
        return mock(RedisTemplate.class);
    }

    @Bean
    @Primary
    @SuppressWarnings("unchecked")
    public ValueOperations<String, Object> mockValueOperations() {
        return mock(ValueOperations.class);
    }
}
