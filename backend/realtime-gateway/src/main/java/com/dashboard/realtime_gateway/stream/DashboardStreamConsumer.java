package com.dashboard.realtime_gateway.stream;

import com.dashboard.realtime_gateway.websocket.DashboardWebSocketHandler;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executors;

@Component
@RequiredArgsConstructor
@Slf4j
public class DashboardStreamConsumer {

    private static final String CONSUMER_NAME = "realtime-gateway-1";

    private final RedisTemplate<String, Object> redisTemplate;
    private final DashboardWebSocketHandler socketHandler;

    @PostConstruct
    public void start() {
        createGroupIfAbsent();
        Executors.newSingleThreadExecutor().submit(this::consume);
    }

    private void createGroupIfAbsent() {
        try {
            redisTemplate.opsForStream().createGroup(
                    StreamConstants.DASHBOARD_STREAM,
                    ReadOffset.from("0"),
                    StreamConstants.REALTIME_GROUP
            );
            log.info("Created consumer group '{}'", StreamConstants.REALTIME_GROUP);
        } catch (Exception e) {
            // group already exists — this is expected on restart
            log.debug("Consumer group already exists: {}", e.getMessage());
        }
    }

    private void consume() {
        while (true) {
            try {
                List<MapRecord<String, Object, Object>> records =
                        redisTemplate.opsForStream().read(
                                Consumer.from(StreamConstants.REALTIME_GROUP, CONSUMER_NAME),
                                StreamReadOptions.empty().block(Duration.ofSeconds(2)),
                                StreamOffset.create(
                                        StreamConstants.DASHBOARD_STREAM,
                                        ReadOffset.lastConsumed()
                                )
                        );

                if (records == null) continue;

                for (MapRecord<String, Object, Object> record : records) {
                    try {
                        socketHandler.broadcast(record.getValue().toString());
                    } catch (Exception e) {
                        log.warn("Failed to broadcast record {}: {}", record.getId(), e.getMessage());
                    }
                    redisTemplate.opsForStream().acknowledge(
                            StreamConstants.DASHBOARD_STREAM,
                            StreamConstants.REALTIME_GROUP,
                            record.getId()
                    );
                }
            } catch (Exception e) {
                log.error("Error consuming stream: {}", e.getMessage());
                try { Thread.sleep(1000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); break; }
            }
        }
    }
}
