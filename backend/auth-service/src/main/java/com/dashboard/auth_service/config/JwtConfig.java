package com.dashboard.auth_service.config;

import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import java.util.Base64;


@Configuration
public class JwtConfig {

    @Bean
    SecretKey jwtSigningKey(
            @Value("${security.jwt.secret:}") String secret
    ) {
        return Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
    }
}