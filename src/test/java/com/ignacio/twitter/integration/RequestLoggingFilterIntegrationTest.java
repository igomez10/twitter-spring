package com.ignacio.twitter.integration;

import com.ignacio.twitter.models.User;
import com.ignacio.twitter.repositories.UserRepository;
import com.ignacio.twitter.services.JwtTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RequestLoggingFilterIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void addsRequestIdHeaderWhenMissing() throws Exception {
        String token = createToken();
        String requestId = mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getHeader("X-Request-Id");

        assertThat(requestId).isNotBlank();
    }

    @Test
    void echoesRequestIdHeaderWhenProvided() throws Exception {
        String token = createToken();
        String requestId = mockMvc.perform(get("/users")
                        .header("X-Request-Id", "test-id-123")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getHeader("X-Request-Id");

        assertThat(requestId).isEqualTo("test-id-123");
    }

    private String createToken() {
        User user = userRepository.save(User.builder()
                .firstName("Request")
                .lastName("Logger")
                .email("logger@example.com")
                .handle("logger")
                .build());
        return jwtTokenService.createToken(user.getHandle(), user.getId(), List.of("user:write"));
    }
}
