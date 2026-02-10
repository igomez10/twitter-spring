package com.ignacio.twitter.middleware;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class JwtAuthFilterTest {

    private static final String SECRET = "test-secret-test-secret-test-secret";

    private JwtAuthFilter filter;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthFilter(SECRET);
        filterChain = mock(FilterChain.class);
    }

    @Test
    void skipsWhenAuthorizationHeaderMissing() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertThat(request.getAttribute(JwtAuthFilter.USER_ID_ATTRIBUTE)).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void setsUserIdWhenTokenIsValid() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        String token = Jwts.builder()
                .subject("42")
                .issuedAt(new Date())
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();

        request.addHeader(JwtAuthFilter.AUTHORIZATION_HEADER, "Bearer " + token);

        filter.doFilter(request, response, filterChain);

        assertThat(request.getAttribute(JwtAuthFilter.USER_ID_ATTRIBUTE)).isEqualTo(42L);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void ignoresNonNumericSubject() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        String token = Jwts.builder()
                .subject("not-a-number")
                .issuedAt(new Date())
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();

        request.addHeader(JwtAuthFilter.AUTHORIZATION_HEADER, "Bearer " + token);

        filter.doFilter(request, response, filterChain);

        assertThat(request.getAttribute(JwtAuthFilter.USER_ID_ATTRIBUTE)).isNull();
    }

    @Test
    void ignoresInvalidToken() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addHeader(JwtAuthFilter.AUTHORIZATION_HEADER, "Bearer invalid.token.here");

        filter.doFilter(request, response, filterChain);

        assertThat(request.getAttribute(JwtAuthFilter.USER_ID_ATTRIBUTE)).isNull();
    }

    @Test
    void requiresSecretConfiguration() {
        assertThrows(IllegalArgumentException.class, () -> new JwtAuthFilter(" "));
    }
}
