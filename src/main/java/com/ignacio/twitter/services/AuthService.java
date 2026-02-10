package com.ignacio.twitter.services;

import com.ignacio.twitter.dto.TokenRequest;
import com.ignacio.twitter.dto.TokenResponse;
import com.ignacio.twitter.models.UserCredential;
import com.ignacio.twitter.repositories.AuthorizationRepository;
import com.ignacio.twitter.repositories.UserCredentialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserCredentialRepository userCredentialRepository;
    private final AuthorizationRepository authorizationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public TokenResponse issueToken(TokenRequest request) {
        UserCredential credential = userCredentialRepository.findByUsername(request.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (!passwordEncoder.matches(request.password(), credential.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        Long userId = credential.getUserId();
        List<String> actions = authorizationRepository.findPermittedActionsByUserId(userId);
        String token = jwtTokenService.createToken(credential.getUsername(), userId, actions);
        return new TokenResponse(token, actions);
    }
}
