package com.ignacio.twitter.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ignacio.twitter.dto.TweetRequest;
import com.ignacio.twitter.dto.UserRequest;
import com.ignacio.twitter.models.Tweet;
import com.ignacio.twitter.models.User;
import com.ignacio.twitter.repositories.TweetRepository;
import com.ignacio.twitter.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiE2eTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TweetRepository tweetRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        tweetRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void userLifecycle() throws Exception {
        UserRequest createRequest = new UserRequest("Ignacio", "Gomez", "ignacio@gomez.com", "nachogomez", "nacho", "password123");

        String response = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        User created = objectMapper.readValue(response, User.class);

        mockMvc.perform(get("/users/" + created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ignacio@gomez.com"));

        mockMvc.perform(delete("/users/" + created.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/users/" + created.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void tweetLifecycle() throws Exception {
        UserRequest userRequest = new UserRequest("Ada", "Lovelace", "ada@lovelace.com", "adal", "adal", "password123");

        String userResponse = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        User createdUser = objectMapper.readValue(userResponse, User.class);

        TweetRequest tweetRequest = new TweetRequest("hello", createdUser.getId());

        String tweetResponse = mockMvc.perform(post("/tweets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tweetRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Tweet createdTweet = objectMapper.readValue(tweetResponse, Tweet.class);

        mockMvc.perform(get("/tweets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(createdTweet.getId().intValue()));

        mockMvc.perform(delete("/tweets/" + createdTweet.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/tweets/" + createdTweet.getId()))
                .andExpect(status().isNotFound());
    }
}
