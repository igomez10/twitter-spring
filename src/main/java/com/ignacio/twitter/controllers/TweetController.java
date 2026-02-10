package com.ignacio.twitter.controllers;

import com.ignacio.twitter.dto.TweetRequest;
import com.ignacio.twitter.models.Tweet;
import com.ignacio.twitter.services.TweetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tweets")
@RequiredArgsConstructor
public class TweetController {

    private final TweetService tweetService;

    @GetMapping
    public List<Tweet> listTweets() {
        return tweetService.listTweets();
    }

    @GetMapping("/{id}")
    public Tweet getTweet(@PathVariable Long id) {
        return tweetService.getTweet(id);
    }

    @PostMapping
    public Tweet createTweet(@Valid @RequestBody TweetRequest request) {
        return tweetService.createTweet(request);
    }

    @PutMapping("/{id}")
    public Tweet updateTweet(@PathVariable Long id, @Valid @RequestBody TweetRequest request) {
        return tweetService.updateTweet(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTweet(@PathVariable Long id) {
        tweetService.deleteTweet(id);
        return ResponseEntity.noContent().build();
    }
}
