package com.ignacio.twitter.middleware;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Optional;

public class JwtAuthFilter extends OncePerRequestFilter {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String USER_ID_ATTRIBUTE = "auth.userId";

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final Key signingKey;

    public JwtAuthFilter(String jwtSecret) {
        this.signingKey = buildSigningKey(jwtSecret);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Optional<String> token = extractBearerToken(request);
        if (token.isPresent()) {
            try {
                Claims claims = Jwts.parser()
                        .setSigningKey(signingKey)
                        .build()
                        .parseClaimsJws(token.get())
                        .getBody();
                String subject = claims.getSubject();
                if (subject != null && !subject.isBlank()) {
                    try {
                        Long userId = Long.parseLong(subject);
                        request.setAttribute(USER_ID_ATTRIBUTE, userId);
                    } catch (NumberFormatException ex) {
                        logger.debug("JWT subject is not a numeric user id: {}", subject);
                    }
                }
            } catch (JwtException ex) {
                logger.debug("Invalid JWT token", ex);
            }
        }

        filterChain.doFilter(request, response);
    }

    private Optional<String> extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION_HEADER);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            return Optional.empty();
        }
        String token = header.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(token);
    }

    private Key buildSigningKey(String jwtSecret) {
        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new IllegalArgumentException("jwt.secret must be configured");
        }
        String trimmed = jwtSecret.trim();
        if (looksLikeBase64(trimmed)) {
            return Keys.hmacShaKeyFor(Decoders.BASE64.decode(trimmed));
        }
        return Keys.hmacShaKeyFor(trimmed.getBytes(StandardCharsets.UTF_8));
    }

    private boolean looksLikeBase64(String value) {
        return value.length() % 4 == 0 && value.matches("^[A-Za-z0-9+/=]+$");
    }
}
