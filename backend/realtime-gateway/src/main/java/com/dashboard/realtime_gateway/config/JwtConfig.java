package com.dashboard.realtime_gateway.config;

import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Base64;

@Configuration
public class JwtConfig {

    @Bean
    public SecretKey jwtSigningKey(
            @Value("${security.jwt.secret}") String secret
    ) {
        return Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
    }
}
