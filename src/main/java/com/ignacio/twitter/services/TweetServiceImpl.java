package com.ignacio.twitter.services;

import com.ignacio.twitter.dto.TweetRequest;
import com.ignacio.twitter.models.Tweet;
import com.ignacio.twitter.models.User;
import com.ignacio.twitter.repositories.TweetRepository;
import com.ignacio.twitter.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TweetServiceImpl implements TweetService {

    private final TweetRepository tweetRepository;
    private final UserRepository userRepository;

    public List<Tweet> listTweets() {
        return tweetRepository.findAllByDeletedAtIsNull();
    }

    public Tweet getTweet(Long id) {
        return tweetRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tweet not found"));
    }

    public Tweet createTweet(TweetRequest request) {
        User author = userRepository.findByIdAndDeletedAtIsNull(request.authorId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        Tweet tweet = Tweet.builder()
                .content(request.content())
                .author(author)
                .timestamp(LocalDateTime.now())
                .build();
        return tweetRepository.save(tweet);
    }

    public Tweet updateTweet(Long id, TweetRequest request) {
        Tweet tweet = getTweet(id);
        User author = userRepository.findByIdAndDeletedAtIsNull(request.authorId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        tweet.setContent(request.content());
        tweet.setAuthor(author);
        return tweetRepository.save(tweet);
    }

    public void deleteTweet(Long id) {
        Tweet tweet = getTweet(id);
        tweet.setDeletedAt(LocalDateTime.now());
        tweetRepository.save(tweet);
    }
}
