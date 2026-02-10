package com.ignacio.twitter.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ignacio.twitter.dto.TweetRequest;
import com.ignacio.twitter.models.Tweet;
import com.ignacio.twitter.models.User;
import com.ignacio.twitter.services.TweetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TweetController.class)
class TweetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TweetService tweetService;

    @Test
    void listTweets_returnsTweets() throws Exception {
        User author = User.builder()
                .id(1L)
                .firstName("Ignacio")
                .lastName("Gomez")
                .email("ignacio@gomez.com")
                .handle("nachogomez")
                .build();
        Tweet tweet = Tweet.builder()
                .id(10L)
                .content("hello")
                .author(author)
                .timestamp(LocalDateTime.now())
                .build();
        when(tweetService.listTweets()).thenReturn(List.of(tweet));

        mockMvc.perform(get("/tweets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].content").value("hello"))
                .andExpect(jsonPath("$[0].author.id").value(1));
    }

    @Test
    void getTweet_returnsTweet() throws Exception {
        User author = User.builder()
                .id(2L)
                .firstName("Ada")
                .lastName("Lovelace")
                .email("ada@lovelace.com")
                .handle("adal")
                .build();
        Tweet tweet = Tweet.builder()
                .id(11L)
                .content("first")
                .author(author)
                .timestamp(LocalDateTime.now())
                .build();
        when(tweetService.getTweet(11L)).thenReturn(tweet);

        mockMvc.perform(get("/tweets/11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(11))
                .andExpect(jsonPath("$.author.id").value(2));
    }

    @Test
    void createTweet_returnsCreatedTweet() throws Exception {
        User author = User.builder()
                .id(3L)
                .firstName("Grace")
                .lastName("Hopper")
                .email("grace@hopper.com")
                .handle("grace")
                .build();
        TweetRequest request = new TweetRequest("hello", 3L);
        Tweet tweet = Tweet.builder()
                .id(12L)
                .content("hello")
                .author(author)
                .timestamp(LocalDateTime.now())
                .build();
        when(tweetService.createTweet(any(TweetRequest.class))).thenReturn(tweet);

        mockMvc.perform(post("/tweets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(12))
                .andExpect(jsonPath("$.author.id").value(3));
    }

    @Test
    void updateTweet_returnsUpdatedTweet() throws Exception {
        User author = User.builder()
                .id(4L)
                .firstName("Linus")
                .lastName("Torvalds")
                .email("linus@linux.org")
                .handle("linus")
                .build();
        TweetRequest request = new TweetRequest("updated", 4L);
        Tweet tweet = Tweet.builder()
                .id(13L)
                .content("updated")
                .author(author)
                .timestamp(LocalDateTime.now())
                .build();
        when(tweetService.updateTweet(eq(13L), any(TweetRequest.class))).thenReturn(tweet);

        mockMvc.perform(put("/tweets/13")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("updated"))
                .andExpect(jsonPath("$.author.id").value(4));
    }

    @Test
    void deleteTweet_returnsNoContent() throws Exception {
        doNothing().when(tweetService).deleteTweet(14L);

        mockMvc.perform(delete("/tweets/14"))
                .andExpect(status().isNoContent());
    }

    @Test
    void createTweet_validationError() throws Exception {
        TweetRequest request = new TweetRequest("", null);

        mockMvc.perform(post("/tweets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
