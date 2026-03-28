package com.dashboard.auth_service.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {}