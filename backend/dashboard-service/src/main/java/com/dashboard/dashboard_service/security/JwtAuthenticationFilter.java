package com.dashboard.dashboard_service.security;

import com.dashboard.dashboard_service.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String uri = request.getRequestURI();
        String header = request.getHeader("Authorization");

        log.debug("Incoming request: {}", uri);

        if (header == null || !header.startsWith("Bearer ")) {
            log.debug("No JWT token found for request {}", uri);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = header.substring(7);
            Claims claims = jwtService.validate(token);

            log.debug("JWT validated. Subject: {}", claims.getSubject());
            String userId = claims.getSubject();
            var auth = new UsernamePasswordAuthenticationToken(
                    claims.getSubject(),
                    null,
                    ((Collection<?>) claims.get("roles")).stream()
                            .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                            .toList()
            );
            request.setAttribute("userId", UUID.fromString(userId));
            SecurityContextHolder.getContext().setAuthentication(auth);
            filterChain.doFilter(request, response);

        } catch (JwtException e) {
            log.warn("Invalid JWT for request {}: {}", uri, e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Invalid or expired token\"}");
        } catch (Exception e) {
            log.error("Unexpected error during JWT processing for request {}", uri, e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Internal server error\"}");
        }
    }
}
