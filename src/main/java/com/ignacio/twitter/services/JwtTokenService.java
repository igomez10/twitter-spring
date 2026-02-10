package com.ignacio.twitter.services;

import com.ignacio.twitter.auth.JwtKeyProvider;
import com.ignacio.twitter.configurations.JwtProperties;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
public class JwtTokenService {

    private final JwtProperties jwtProperties;
    private Key signingKey;

    public JwtTokenService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @PostConstruct
    void init() {
        this.signingKey = JwtKeyProvider.buildSigningKey(jwtProperties.secret());
    }

    public String createToken(String username, Long userId, List<String> actions) {
        Instant now = Instant.now();
        List<String> safeActions = actions == null ? List.of() : actions;
        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("actions", safeActions)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(jwtProperties.ttlSeconds())))
                .signWith(signingKey)
                .compact();
    }
}
