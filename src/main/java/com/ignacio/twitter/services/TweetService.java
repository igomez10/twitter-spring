package com.ignacio.twitter.services;

import com.ignacio.twitter.dto.TweetRequest;
import com.ignacio.twitter.models.Tweet;

import java.util.List;

public interface TweetService {
    List<Tweet> listTweets();

    Tweet getTweet(Long id);

    Tweet createTweet(TweetRequest request, Long actorUserId);

    Tweet updateTweet(Long id, TweetRequest request, Long actorUserId);

    void deleteTweet(Long id, Long actorUserId);
}
