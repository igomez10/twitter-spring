package com.ignacio.twitter.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ignacio.twitter.dto.TweetRequest;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TweetControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TweetRepository tweetRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        tweetRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createAndGetTweet() throws Exception {
        User author = userRepository.save(User.builder()
                .firstName("Ignacio")
                .lastName("Gomez")
                .email("ignacio@gomez.com")
                .handle("nachogomez")
                .build());
        TweetRequest request = new TweetRequest("hello", author.getId());

        String response = mockMvc.perform(post("/tweets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Tweet created = objectMapper.readValue(response, Tweet.class);

        mockMvc.perform(get("/tweets/" + created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("hello"))
                .andExpect(jsonPath("$.author.id").value(author.getId()));
    }

    @Test
    void updateTweet() throws Exception {
        User author = userRepository.save(User.builder()
                .firstName("Ada")
                .lastName("Lovelace")
                .email("ada@lovelace.com")
                .handle("adal")
                .build());
        Tweet tweet = tweetRepository.save(Tweet.builder()
                .content("old")
                .author(author)
                .build());

        TweetRequest request = new TweetRequest("updated", author.getId());

        mockMvc.perform(put("/tweets/" + tweet.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("updated"));
    }

    @Test
    void deleteTweet_softDelete() throws Exception {
        User author = userRepository.save(User.builder()
                .firstName("Grace")
                .lastName("Hopper")
                .email("grace@hopper.com")
                .handle("grace")
                .build());
        Tweet tweet = tweetRepository.save(Tweet.builder()
                .content("hello")
                .author(author)
                .build());

        mockMvc.perform(delete("/tweets/" + tweet.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/tweets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/tweets/" + tweet.getId()))
                .andExpect(status().isNotFound());

        Tweet stored = tweetRepository.findById(tweet.getId()).orElseThrow();
        assertThat(stored.getDeletedAt()).isNotNull();
    }
}
