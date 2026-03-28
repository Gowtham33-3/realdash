package com.dashboard.dashboard_service.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;


@Service
public class JwtService {

    private final SecretKey key;

    public JwtService(SecretKey key) {
        this.key = key;
    }

    public Claims validate(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
