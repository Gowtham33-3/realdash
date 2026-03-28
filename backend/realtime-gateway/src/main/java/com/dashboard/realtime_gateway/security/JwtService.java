package com.dashboard.realtime_gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {

    private final SecretKey key;

    public Claims validate(String token) {
        try {

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            log.info("JWT validated for user={}", claims.getSubject());

            return claims;

        } catch (Exception e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            throw e;
        }
    }
}