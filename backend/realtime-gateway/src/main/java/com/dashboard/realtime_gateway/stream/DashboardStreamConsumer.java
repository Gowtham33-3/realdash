package com.dashboard.realtime_gateway.stream;

import com.dashboard.realtime_gateway.websocket.DashboardWebSocketHandler;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;

@Component
@RequiredArgsConstructor
@Slf4j
public class DashboardStreamConsumer {

    private final RedisTemplate<String, Object> redisTemplate;
    private final DashboardWebSocketHandler socketHandler;

    @PostConstruct
    public void start() {
        Executors.newSingleThreadExecutor().submit(this::consume);
    }

    private void consume() {
        while (true) {
            List<MapRecord<String, Object, Object>> records =
                    redisTemplate.opsForStream().read(
                            Consumer.from(
                                    StreamConstants.REALTIME_GROUP,
                                    UUID.randomUUID().toString()
                            ),
                            StreamReadOptions.empty().block(Duration.ofSeconds(2)),
                            StreamOffset.create(
                                    StreamConstants.DASHBOARD_STREAM,
                                    ReadOffset.lastConsumed()
                            )
                    );

            if (records == null) continue;

            for (MapRecord<String, Object, Object> record : records) {
                socketHandler.broadcast(record.getValue().toString());

                redisTemplate.opsForStream().acknowledge(
                        StreamConstants.DASHBOARD_STREAM,
                        StreamConstants.REALTIME_GROUP,
                        record.getId()
                );
            }
        }
    }
}