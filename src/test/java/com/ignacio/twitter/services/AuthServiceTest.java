package com.ignacio.twitter.services;

import com.ignacio.twitter.dto.TokenRequest;
import com.ignacio.twitter.dto.TokenResponse;
import com.ignacio.twitter.models.UserCredential;
import com.ignacio.twitter.repositories.AuthorizationRepository;
import com.ignacio.twitter.repositories.UserCredentialRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserCredentialRepository userCredentialRepository;

    @Mock
    private AuthorizationRepository authorizationRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenService jwtTokenService;

    @InjectMocks
    private AuthService authService;

    @Test
    void issueToken_returnsTokenAndActions() {
        UserCredential credential = UserCredential.builder()
                .userId(10L)
                .username("user")
                .passwordHash("hash")
                .passwordSalt("$2a$10$saltsaltsaltsalt")
                .build();
        when(userCredentialRepository.findByUsername("user")).thenReturn(Optional.of(credential));
        when(passwordEncoder.matches("pass", "hash")).thenReturn(true);
        when(authorizationRepository.findPermittedActionsByUserId(10L))
                .thenReturn(List.of("tweet:read"));
        when(jwtTokenService.createToken("user", 10L, List.of("tweet:read"))).thenReturn("token");

        TokenResponse response = authService.issueToken(new TokenRequest("user", "pass"));

        assertThat(response.accessToken()).isEqualTo("token");
        assertThat(response.actions()).containsExactly("tweet:read");
    }

    @Test
    void issueToken_invalidPassword_throwsUnauthorized() {
        UserCredential credential = UserCredential.builder()
                .userId(11L)
                .username("user")
                .passwordHash("hash")
                .passwordSalt("$2a$10$saltsaltsaltsalt")
                .build();
        when(userCredentialRepository.findByUsername("user")).thenReturn(Optional.of(credential));
        when(passwordEncoder.matches("bad", "hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.issueToken(new TokenRequest("user", "bad")))
                .isInstanceOf(ResponseStatusException.class);
    }
}
