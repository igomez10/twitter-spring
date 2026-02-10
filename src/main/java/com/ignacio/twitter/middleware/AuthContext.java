package com.ignacio.twitter.middleware;

import com.ignacio.twitter.auth.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class AuthContext {

    private AuthContext() {
    }

    public static Optional<Long> getUserId(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser user) {
            return Optional.ofNullable(user.userId());
        }
        return Optional.empty();
    }

    public static List<String> getActions(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser user) {
            return user.actions();
        }
        return Collections.emptyList();
    }
}
