package com.ignacio.twitter.services;

import com.ignacio.twitter.dto.TweetRequest;
import com.ignacio.twitter.auth.AuthenticatedUser;
import com.ignacio.twitter.models.Event;
import com.ignacio.twitter.models.EventType;
import com.ignacio.twitter.models.Tweet;
import com.ignacio.twitter.models.User;
import com.ignacio.twitter.repositories.EventRepository;
import com.ignacio.twitter.repositories.TweetRepository;
import com.ignacio.twitter.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TweetServiceImpl implements TweetService {

    private static final String ENTITY_TYPE_TWEET = "tweet";

    private final TweetRepository tweetRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public List<Tweet> listTweets() {
        return tweetRepository.findAllByDeletedAtIsNull();
    }

    public Tweet getTweet(Long id) {
        return tweetRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tweet not found"));
    }

    @Transactional
    public Tweet createTweet(TweetRequest request) {
        Long actorUserId = resolveActorUserId();
        User author = userRepository.findByIdAndDeletedAtIsNull(request.authorId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        Tweet tweet = Tweet.builder()
                .content(request.content())
                .author(author)
                .timestamp(LocalDateTime.now())
                .build();
        Tweet created = tweetRepository.save(tweet);
        eventRepository.save(buildEvent(EventType.TWEET_CREATED, created.getId(), actorUserId));
        return created;
    }

    @Transactional
    public Tweet updateTweet(Long id, TweetRequest request) {
        Long actorUserId = resolveActorUserId();
        Tweet tweet = getTweet(id);
        User author = userRepository.findByIdAndDeletedAtIsNull(request.authorId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        tweet.setContent(request.content());
        tweet.setAuthor(author);
        Tweet updated = tweetRepository.save(tweet);
        eventRepository.save(buildEvent(EventType.TWEET_UPDATED, updated.getId(), actorUserId));
        return updated;
    }

    @Transactional
    public void deleteTweet(Long id) {
        Long actorUserId = resolveActorUserId();
        Tweet tweet = getTweet(id);
        tweet.setDeletedAt(LocalDateTime.now());
        tweetRepository.save(tweet);
        eventRepository.save(buildEvent(EventType.TWEET_DELETED, tweet.getId(), actorUserId));
    }

    private Event buildEvent(EventType eventType, Long entityId, Long actorUserId) {
        return Event.builder()
                .eventType(eventType)
                .entityType(ENTITY_TYPE_TWEET)
                .entityId(entityId)
                .actorUserId(actorUserId)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private Long resolveActorUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthenticatedUser authenticatedUser) {
            return authenticatedUser.userId();
        }
        return null;
    }
}
