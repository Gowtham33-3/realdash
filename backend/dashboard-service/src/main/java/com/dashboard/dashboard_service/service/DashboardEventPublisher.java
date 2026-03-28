package com.dashboard.dashboard_service.service;

import com.dashboard.dashboard_service.stream.StreamConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardEventPublisher {

    private final RedisTemplate<String, Object> redisTemplate;

    public void publish(
            String eventType,
            UUID dashboardId,
            Object payload
    ) {
        Map<String, Object> event = Map.of(
                "eventId", UUID.randomUUID().toString(),
                "eventType", eventType,
                "aggregateType", "DASHBOARD",
                "aggregateId", dashboardId.toString(),
                "version", 1,
                "timestamp", System.currentTimeMillis(),
                "payload", payload
        );

        redisTemplate.opsForStream().add(
                StreamRecords.mapBacked(event)
                        .withStreamKey(StreamConstants.DASHBOARD_STREAM)
        );
    }
}