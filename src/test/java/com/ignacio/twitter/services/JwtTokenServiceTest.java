package com.ignacio.twitter.services;

import com.ignacio.twitter.auth.JwtKeyProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;

import java.security.Key;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenServiceTest {

    @Test
    void createToken_includesActionsAndExpiry() {
        String secret = "test-secret-test-secret-test-secret";
        JwtTokenService service = new JwtTokenService(secret, 60);
        service.init();

        String token = service.createToken("adal", 7L, List.of("tweet:read"));

        Key key = JwtKeyProvider.buildSigningKey(secret);
        Claims claims = Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        @SuppressWarnings("unchecked")
        List<String> actions = (List<String>) claims.get("actions");

        assertThat(claims.getSubject()).isEqualTo("adal");
        assertThat(((Number) claims.get("userId")).longValue()).isEqualTo(7L);
        assertThat(actions).contains("tweet:read");
        assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
    }
}
