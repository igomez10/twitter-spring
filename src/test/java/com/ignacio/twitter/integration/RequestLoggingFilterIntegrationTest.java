package com.ignacio.twitter.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RequestLoggingFilterIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void addsRequestIdHeaderWhenMissing() throws Exception {
        String requestId = mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getHeader("X-Request-Id");

        assertThat(requestId).isNotBlank();
    }

    @Test
    void echoesRequestIdHeaderWhenProvided() throws Exception {
        String requestId = mockMvc.perform(get("/users").header("X-Request-Id", "test-id-123"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getHeader("X-Request-Id");

        assertThat(requestId).isEqualTo("test-id-123");
    }
}
