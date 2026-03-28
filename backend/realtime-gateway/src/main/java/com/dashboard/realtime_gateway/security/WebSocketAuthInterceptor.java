package com.dashboard.realtime_gateway.security;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

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
        log.info("WebSocket handshake from {}", request.getRemoteAddress());

        String token = extractToken(request);

        if (token == null) {
            log.warn("WebSocket handshake rejected: no token found");
            return false;
        }

        try {
            Claims claims = jwtService.validate(token);
            attributes.put("userId", claims.getSubject());
            log.info("WebSocket authenticated userId={}", claims.getSubject());
            return true;
        } catch (Exception e) {
            log.warn("WebSocket handshake rejected: {}", e.getMessage());
            return false;
        }
    }

    private String extractToken(ServerHttpRequest request) {
        // 1. try query param ?token=...  (browsers can't set WS headers)
        String query = request.getURI().getQuery();
        if (query != null) {
            for (String param : query.split("&")) {
                if (param.startsWith("token=")) {
                    return param.substring(6);
                }
            }
        }

        // 2. fallback to Authorization header
        var authHeaders = request.getHeaders().get("Authorization");
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String header = authHeaders.get(0);
            if (header.startsWith("Bearer ")) return header.substring(7);
        }

        return null;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler handler, Exception ex) {
        if (ex != null) log.error("Handshake error", ex);
    }
}
