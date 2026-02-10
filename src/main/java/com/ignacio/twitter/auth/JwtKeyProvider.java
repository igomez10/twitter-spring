package com.ignacio.twitter.auth;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;

public final class JwtKeyProvider {

    private JwtKeyProvider() {
    }

    public static Key buildSigningKey(String jwtSecret) {
        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new IllegalArgumentException("jwt.secret must be configured");
        }
        String trimmed = jwtSecret.trim();
        if (looksLikeBase64(trimmed)) {
            return Keys.hmacShaKeyFor(Decoders.BASE64.decode(trimmed));
        }
        return Keys.hmacShaKeyFor(trimmed.getBytes(StandardCharsets.UTF_8));
    }

    private static boolean looksLikeBase64(String value) {
        return value.length() % 4 == 0 && value.matches("^[A-Za-z0-9+/=]+$");
    }
}
