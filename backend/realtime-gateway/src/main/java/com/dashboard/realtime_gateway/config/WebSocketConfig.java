package com.dashboard.realtime_gateway.config;


import com.dashboard.realtime_gateway.security.WebSocketAuthInterceptor;
import com.dashboard.realtime_gateway.websocket.DashboardWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Slf4j
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final DashboardWebSocketHandler handler;
    private final WebSocketAuthInterceptor authInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {

        log.info("Registering WebSocket endpoint /ws");

        registry.addHandler(handler, "/ws/dashboard/*")
                .addInterceptors(authInterceptor)
                .setAllowedOriginPatterns("*");
    }
}