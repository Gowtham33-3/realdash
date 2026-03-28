package com.dashboard.realtime_gateway.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@Slf4j
public class DashboardWebSocketHandler extends TextWebSocketHandler {

    private final Map<String, List<WebSocketSession>> sessions =
            new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String userId = session.getAttributes().get("userId").toString();
        sessions
                .computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>())
                .add(session);
    }

    @Override
    public void afterConnectionClosed(
            WebSocketSession session,
            CloseStatus status
    ) {
        sessions.values().forEach(list -> list.remove(session));
    }

    public void broadcast(String message) {
        sessions.values().forEach(list ->
                list.forEach(session -> {
                    try {
                        session.sendMessage(new TextMessage(message));
                    } catch (Exception e) {
                        log.warn("WS send failed", e);
                    }
                })
        );
    }
}