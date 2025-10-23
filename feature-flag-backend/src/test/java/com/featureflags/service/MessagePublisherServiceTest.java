package com.featureflags.service;

import com.featureflags.config.UnitTestConfig;
import com.featureflags.entity.FeatureFlag;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Import(UnitTestConfig.class)
class MessagePublisherServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private MessagePublisherService messagePublisherService;

    private FeatureFlag testFlag;

    @BeforeEach
    void setUp() {
        testFlag = new FeatureFlag();
        testFlag.setId(1L);
        testFlag.setName("test_flag");
        testFlag.setDescription("A test feature flag");
        testFlag.setEnabled(true);
    }

    @Test
    void testPublishFlagCreated_Success() throws Exception {
        // Given
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"eventType\":\"CREATED\"}");

        // When
        messagePublisherService.publishFlagCreated(testFlag);

        // Then
        verify(objectMapper).writeValueAsString(any());
        verify(redisTemplate).convertAndSend(anyString(), anyString());
    }

    @Test
    void testPublishFlagUpdated_Success() throws Exception {
        // Given
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"eventType\":\"UPDATED\"}");

        // When
        messagePublisherService.publishFlagUpdated(testFlag);

        // Then
        verify(objectMapper).writeValueAsString(any());
        verify(redisTemplate).convertAndSend(anyString(), anyString());
    }

    @Test
    void testPublishFlagDeleted_Success() throws Exception {
        // Given
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"eventType\":\"DELETED\"}");

        // When
        messagePublisherService.publishFlagDeleted(testFlag);

        // Then
        verify(objectMapper).writeValueAsString(any());
        verify(redisTemplate).convertAndSend(anyString(), anyString());
    }
}
