package com.dashboard.auth_service.service;

import com.dashboard.auth_service.model.RefreshToken;
import com.dashboard.auth_service.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository repository;

    public RefreshTokenService(RefreshTokenRepository repository) {
        this.repository = repository;
    }

    public RefreshToken create(UUID userId) {
        RefreshToken token = new RefreshToken();
        token.setId(UUID.randomUUID());
        token.setUserId(userId);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiresAt(Instant.now().plus(30, ChronoUnit.DAYS));
        token.setRevoked(false);
        return repository.save(token);
    }

    public RefreshToken validate(String token) {
        RefreshToken refreshToken = repository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (refreshToken.isRevoked()
                || refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("Refresh token expired or revoked");
        }

        return refreshToken;
    }

    public void revoke(String token) {
        repository.findByToken(token).ifPresent(rt -> {
            rt.setRevoked(true);
            repository.save(rt);
        });
    }
}