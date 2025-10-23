package com.featureflags.util;

import java.util.UUID;

/**
 * Simple message ID generator for feature flag events.
 * Uses UUID for global uniqueness and simplicity.
 */
public class MessageIdGenerator {

    /**
     * Generate a unique message ID using UUID.
     * Format: "flag-{timestamp}-{uuid}"
     * Example: "flag-1703123456789-550e8400-e29b-41d4-a716-446655440000"
     */
    public static String generateMessageId(String flagName) {
        long timestamp = System.currentTimeMillis();
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return String.format("flag-%s-%d-%s", flagName, timestamp, uuid);
    }

    /**
     * Generate a simple UUID-based message ID.
     * Format: "msg-{uuid}"
     * Example: "msg-550e8400e29b41d4a716446655440000"
     */
    public static String generateSimpleMessageId() {
        return "msg-" + UUID.randomUUID().toString().replace("-", "");
    }

}
