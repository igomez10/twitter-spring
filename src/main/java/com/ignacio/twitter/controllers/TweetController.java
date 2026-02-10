package com.ignacio.twitter.controllers;

import com.ignacio.twitter.dto.TweetRequest;
import com.ignacio.twitter.auth.AuthenticatedUser;
import com.ignacio.twitter.models.Tweet;
import com.ignacio.twitter.services.TweetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    @PreAuthorize("hasAuthority('tweet:read') or hasAuthority('tweet:write')")
    public List<Tweet> listTweets() {
        return tweetService.listTweets();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('tweet:read') or hasAuthority('tweet:write')")
    public Tweet getTweet(@PathVariable Long id) {
        return tweetService.getTweet(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('tweet:write')")
    public Tweet createTweet(@Valid @RequestBody TweetRequest request,
                             @AuthenticationPrincipal AuthenticatedUser principal) {
        Long actorUserId = principal != null ? principal.userId() : null;
        return tweetService.createTweet(request, actorUserId);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('tweet:write')")
    public Tweet updateTweet(@PathVariable Long id, @Valid @RequestBody TweetRequest request,
                             @AuthenticationPrincipal AuthenticatedUser principal) {
        Long actorUserId = principal != null ? principal.userId() : null;
        return tweetService.updateTweet(id, request, actorUserId);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('tweet:write')")
    public ResponseEntity<Void> deleteTweet(@PathVariable Long id,
                                            @AuthenticationPrincipal AuthenticatedUser principal) {
        Long actorUserId = principal != null ? principal.userId() : null;
        tweetService.deleteTweet(id, actorUserId);
        return ResponseEntity.noContent().build();
    }
}
