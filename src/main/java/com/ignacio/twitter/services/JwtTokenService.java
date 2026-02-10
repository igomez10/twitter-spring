package com.ignacio.twitter.services;

import com.ignacio.twitter.auth.JwtKeyProvider;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
public class JwtTokenService {

    private final String jwtSecret;
    private final long ttlSeconds;
    private Key signingKey;

    public JwtTokenService(
            @Value("${jwt.secret}") String jwtSecret,
            @Value("${jwt.ttlSeconds:3600}") long ttlSeconds
    ) {
        this.jwtSecret = jwtSecret;
        this.ttlSeconds = ttlSeconds;
    }

    @PostConstruct
    void init() {
        this.signingKey = JwtKeyProvider.buildSigningKey(jwtSecret);
    }

    public String createToken(String username, Long userId, List<String> actions) {
        Instant now = Instant.now();
        List<String> safeActions = actions == null ? List.of() : actions;
        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("actions", safeActions)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(ttlSeconds)))
                .signWith(signingKey)
                .compact();
    }
}
