package com.dashboard.realtime_gateway.security;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private final JwtService jwtService;

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler handler,
            Map<String, Object> attributes
    ) {

        log.info("WebSocket handshake initiated from {}", request.getRemoteAddress());

        List<String> authHeaders = request.getHeaders().get("Authorization");

        if (authHeaders == null || authHeaders.isEmpty()) {
            log.warn("WebSocket handshake rejected: Authorization header missing");
            return false;
        }

        String header = authHeaders.get(0);

        if (!header.startsWith("Bearer ")) {
            log.warn("WebSocket handshake rejected: Invalid Authorization header format");
            return false;
        }

        try {
            String token = header.substring(7);
            Claims claims = jwtService.validate(token);

            String userId = claims.getSubject();
            attributes.put("userId", userId);

            log.info("WebSocket authentication successful for userId={}", userId);

            return true;

        } catch (Exception e) {
            log.error("WebSocket handshake rejected: JWT validation failed", e);
            return false;
        }
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler handler,
            Exception ex
    ) {

        if (ex != null) {
            log.error("WebSocket handshake completed with exception", ex);
        } else {
            log.info("WebSocket handshake completed successfully");
        }
    }
}