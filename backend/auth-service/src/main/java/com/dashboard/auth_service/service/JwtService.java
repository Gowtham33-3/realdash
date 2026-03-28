package com.dashboard.auth_service.service;

import com.dashboard.auth_service.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey key;

    public JwtService(SecretKey key) {
        this.key = key;
    }

    public String generateAccessToken(User user) {
        return Jwts.builder()
                .setSubject(user.getId().toString())
                .claim("roles", user.getRoles())
                .setIssuedAt(new Date())
                .setExpiration(
                        Date.from(Instant.now().plus(15, ChronoUnit.MINUTES))
                )
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims validate(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
