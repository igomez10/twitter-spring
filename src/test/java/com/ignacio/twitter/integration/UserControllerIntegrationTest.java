package com.ignacio.twitter.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ignacio.twitter.dto.UserRequest;
import com.ignacio.twitter.models.User;
import com.ignacio.twitter.repositories.TweetRepository;
import com.ignacio.twitter.repositories.UserRepository;
import com.ignacio.twitter.services.JwtTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TweetRepository tweetRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() {
        tweetRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createAndGetUser() throws Exception {
        UserRequest request = new UserRequest("Ignacio", "Gomez", "ignacio@gomez.com", "nachogomez", "nacho", "password123");

        String response = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        User created = objectMapper.readValue(response, User.class);
        String token = createToken(created, List.of("user:write"));

        mockMvc.perform(get("/users/" + created.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ignacio@gomez.com"));
    }

    @Test
    void updateUser() throws Exception {
        User user = userRepository.save(User.builder()
                .firstName("Old")
                .lastName("Name")
                .email("old@x.com")
                .handle("old")
                .build());
        String token = createToken(user, List.of("user:write"));

        UserRequest request = new UserRequest("New", "Name", "new@x.com", "new", "newuser", "newpassword");

        mockMvc.perform(put("/users/" + user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("new@x.com"))
                .andExpect(jsonPath("$.handle").value("new"));
    }

    @Test
    void deleteUser_softDelete() throws Exception {
        User user = userRepository.save(User.builder()
                .firstName("Ignacio")
                .lastName("Gomez")
                .email("ignacio@gomez.com")
                .handle("nachogomez")
                .build());
        String token = createToken(user, List.of("user:write"));

        mockMvc.perform(delete("/users/" + user.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/users/" + user.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());

        User stored = userRepository.findById(user.getId()).orElseThrow();
        assertThat(stored.getDeletedAt()).isNotNull();
    }

    private String createToken(User user, List<String> actions) {
        return jwtTokenService.createToken(user.getHandle(), user.getId(), actions);
    }
}
