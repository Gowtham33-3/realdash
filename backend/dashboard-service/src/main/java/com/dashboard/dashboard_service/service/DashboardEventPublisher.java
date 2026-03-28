package com.dashboard.dashboard_service.service;

import com.dashboard.dashboard_service.stream.StreamConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardEventPublisher {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public void publish(String eventType, UUID dashboardId, Object payload) {
        try {
            String payloadJson = objectMapper.writeValueAsString(payload);

            Map<String, Object> event = Map.of(
                    "eventId", UUID.randomUUID().toString(),
                    "eventType", eventType,
                    "aggregateType", "DASHBOARD",
                    "aggregateId", dashboardId.toString(),
                    "version", "1",
                    "timestamp", String.valueOf(System.currentTimeMillis()),
                    "payload", payloadJson
            );

            redisTemplate.opsForStream().add(
                    StreamRecords.mapBacked(event)
                            .withStreamKey(StreamConstants.DASHBOARD_STREAM)
            );
        } catch (Exception e) {
            log.error("Failed to publish event {} for dashboard {}: {}", eventType, dashboardId, e.getMessage());
        }
    }
}
