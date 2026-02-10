package com.ignacio.twitter.middleware;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import com.ignacio.twitter.auth.AuthenticatedUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtAuthFilterTest {

    private static final String SECRET = "test-secret-test-secret-test-secret";

    private JwtAuthFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthFilter(SECRET);
        SecurityContextHolder.clearContext();
    }

    @Test
    void skipsWhenAuthorizationHeaderMissing() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<Authentication> capturedAuth = new AtomicReference<>();
        FilterChain filterChain = (req, res) -> capturedAuth.set(SecurityContextHolder.getContext().getAuthentication());

        filter.doFilter(request, response, filterChain);

        assertThat(capturedAuth.get()).isNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void setsUserIdWhenTokenIsValid() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<Authentication> capturedAuth = new AtomicReference<>();
        FilterChain filterChain = (req, res) -> capturedAuth.set(SecurityContextHolder.getContext().getAuthentication());

        String token = Jwts.builder()
                .subject("adal")
                .claim("userId", 42L)
                .claim("actions", java.util.List.of("tweet:read", "tweet:write"))
                .issuedAt(new Date())
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();

        request.addHeader(JwtAuthFilter.AUTHORIZATION_HEADER, "Bearer " + token);

        filter.doFilter(request, response, filterChain);

        Authentication authentication = capturedAuth.get();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isInstanceOf(AuthenticatedUser.class);
        AuthenticatedUser principal = (AuthenticatedUser) authentication.getPrincipal();
        assertThat(principal.userId()).isEqualTo(42L);
        assertThat(principal.actions()).containsExactly("tweet:read", "tweet:write");
        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactlyInAnyOrder("tweet:read", "tweet:write");
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void ignoresTokenWithoutUserIdClaim() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<Authentication> capturedAuth = new AtomicReference<>();
        FilterChain filterChain = (req, res) -> capturedAuth.set(SecurityContextHolder.getContext().getAuthentication());

        String token = Jwts.builder()
                .subject("not-a-number")
                .issuedAt(new Date())
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();

        request.addHeader(JwtAuthFilter.AUTHORIZATION_HEADER, "Bearer " + token);

        filter.doFilter(request, response, filterChain);

        assertThat(capturedAuth.get()).isNull();
    }

    @Test
    void ignoresInvalidToken() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<Authentication> capturedAuth = new AtomicReference<>();
        FilterChain filterChain = (req, res) -> capturedAuth.set(SecurityContextHolder.getContext().getAuthentication());

        request.addHeader(JwtAuthFilter.AUTHORIZATION_HEADER, "Bearer invalid.token.here");

        filter.doFilter(request, response, filterChain);

        assertThat(capturedAuth.get()).isNull();
    }

    @Test
    void requiresSecretConfiguration() {
        assertThrows(IllegalArgumentException.class, () -> new JwtAuthFilter(" "));
    }
}
