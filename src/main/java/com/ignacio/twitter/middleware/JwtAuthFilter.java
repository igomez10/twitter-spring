package com.ignacio.twitter.middleware;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import com.ignacio.twitter.auth.AuthenticatedUser;
import com.ignacio.twitter.auth.JwtKeyProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.Key;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

public class JwtAuthFilter extends OncePerRequestFilter {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final Key signingKey;

    public JwtAuthFilter(String jwtSecret) {
        this.signingKey = JwtKeyProvider.buildSigningKey(jwtSecret);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Optional<String> token = extractBearerToken(request);
        Authentication previousAuth = SecurityContextHolder.getContext().getAuthentication();
        boolean authenticationSet = false;
        if (token.isPresent()) {
            try {
                Claims claims = Jwts.parser()
                        .setSigningKey(signingKey)
                        .build()
                        .parseClaimsJws(token.get())
                        .getBody();
                Long userId = extractUserId(claims);
                if (userId != null) {
                    List<String> actions = extractActions(claims);
                    Authentication authentication = buildAuthentication(userId, actions);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    authenticationSet = true;
                } else {
                    String subject = claims.getSubject();
                    if (subject != null && !subject.isBlank()) {
                        logger.debug("JWT does not include a numeric user id claim; subject={}", subject);
                    }
                }
            } catch (JwtException ex) {
                logger.debug("Invalid JWT token", ex);
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            if (authenticationSet) {
                SecurityContextHolder.clearContext();
                if (previousAuth != null) {
                    SecurityContextHolder.getContext().setAuthentication(previousAuth);
                }
            }
        }
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

    private List<String> extractActions(Claims claims) {
        Object value = claims.get("actions");
        if (value instanceof List<?> list) {
            return list.stream()
                    .filter(item -> item instanceof String)
                    .map(Object::toString)
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    private Long extractUserId(Claims claims) {
        Object value = claims.get("userId");
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            try {
                return Long.parseLong(text);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
        String subject = claims.getSubject();
        if (subject != null && !subject.isBlank()) {
            try {
                return Long.parseLong(subject);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
        return null;
    }

    private Authentication buildAuthentication(Long userId, List<String> actions) {
        List<SimpleGrantedAuthority> authorities = actions.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
        AuthenticatedUser principal = new AuthenticatedUser(userId, actions);
        return new UsernamePasswordAuthenticationToken(principal, null, authorities);
    }
}
