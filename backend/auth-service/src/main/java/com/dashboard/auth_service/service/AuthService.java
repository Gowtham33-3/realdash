package com.dashboard.auth_service.service;

import com.dashboard.auth_service.dto.LoginRequest;
import com.dashboard.auth_service.dto.RefreshTokenRequest;
import com.dashboard.auth_service.dto.SignupRequest;
import com.dashboard.auth_service.dto.TokenResponse;
import com.dashboard.auth_service.model.RefreshToken;
import com.dashboard.auth_service.model.User;
import com.dashboard.auth_service.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.*;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;


    public void signup(SignupRequest request) {

        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username already exists");
        }

        User user = User.builder()
                .id(UUID.randomUUID())
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .roles(Set.of("USER"))
                .build();

        userRepository.save(user);
    }

    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(
                request.password(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.create(user.getId());

        return new TokenResponse(accessToken, refreshToken.getToken());
    }

    public TokenResponse refresh(String refreshToken) {
        RefreshToken rt = refreshTokenService.validate(refreshToken);
        User user = userRepository.findById(rt.getUserId()).orElseThrow();

        String accessToken = jwtService.generateAccessToken(user);
        return new TokenResponse(accessToken, rt.getToken());
    }

    public void logout(String refreshToken) {
        refreshTokenService.revoke(refreshToken);
    }
}