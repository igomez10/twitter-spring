package com.ignacio.twitter.middleware;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

public final class AuthContext {

    private AuthContext() {
    }

    public static Optional<Long> getUserId(HttpServletRequest request) {
        Object value = request.getAttribute(JwtAuthFilter.USER_ID_ATTRIBUTE);
        if (value instanceof Long userId) {
            return Optional.of(userId);
        }
        return Optional.empty();
    }
}
