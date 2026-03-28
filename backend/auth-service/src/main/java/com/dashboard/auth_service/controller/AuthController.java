package com.dashboard.auth_service.controller;

import com.dashboard.auth_service.dto.LoginRequest;
import com.dashboard.auth_service.dto.RefreshTokenRequest;
import com.dashboard.auth_service.dto.SignupRequest;
import com.dashboard.auth_service.dto.TokenResponse;
import com.dashboard.auth_service.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    private ObjectMapper mapper;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }


    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public void signup(@RequestBody @Valid SignupRequest request) {
        authService.signup(request);
    }
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
            @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {

        TokenResponse tokenResponse = authService.login(request);

        Cookie cookie = new Cookie("refreshToken", tokenResponse.refreshToken());
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/auth/refresh");
        cookie.setMaxAge(7 * 24 * 60 * 60);

        response.addCookie(cookie);

        return ResponseEntity.ok(
                new TokenResponse(tokenResponse.accessToken(), null)
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @CookieValue("refreshToken") String refreshToken,
            HttpServletResponse response
    ) {

        TokenResponse tokens = authService.refresh(refreshToken);

        Cookie cookie = new Cookie("refreshToken", tokens.refreshToken());
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/auth/refresh");
        cookie.setMaxAge(7 * 24 * 60 * 60);

        response.addCookie(cookie);

        return ResponseEntity.ok(
                new TokenResponse(tokens.accessToken(), null)
        );
    }

    @PostMapping("/logout")
    public void logout(
            @RequestBody @Valid RefreshTokenRequest request
    ) {
        authService.logout(request.refreshToken());
    }
}