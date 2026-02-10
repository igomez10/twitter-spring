package com.ignacio.twitter.services;

import com.ignacio.twitter.dto.TweetRequest;
import com.ignacio.twitter.models.Tweet;
import com.ignacio.twitter.models.User;
import com.ignacio.twitter.repositories.TweetRepository;
import com.ignacio.twitter.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TweetServiceImplTest {

    @Mock
    private TweetRepository tweetRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TweetServiceImpl tweetService;

    @Test
    void listTweets_returnsNonDeleted() {
        Tweet tweet = Tweet.builder().id(1L).content("hello").build();
        when(tweetRepository.findAllByDeletedAtIsNull()).thenReturn(List.of(tweet));

        List<Tweet> tweets = tweetService.listTweets();

        assertThat(tweets).hasSize(1);
        verify(tweetRepository).findAllByDeletedAtIsNull();
    }

    @Test
    void getTweet_returnsTweet() {
        Tweet tweet = Tweet.builder().id(2L).content("hello").build();
        when(tweetRepository.findByIdAndDeletedAtIsNull(2L)).thenReturn(Optional.of(tweet));

        Tweet result = tweetService.getTweet(2L);

        assertThat(result.getId()).isEqualTo(2L);
    }

    @Test
    void getTweet_missing_throws() {
        when(tweetRepository.findByIdAndDeletedAtIsNull(3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tweetService.getTweet(3L))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void createTweet_savesTweet() {
        User author = User.builder().id(10L).email("a@b.com").handle("ab").build();
        when(userRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(author));
        when(tweetRepository.save(org.mockito.ArgumentMatchers.any(Tweet.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TweetRequest request = new TweetRequest("hello", 10L);
        Tweet created = tweetService.createTweet(request);

        assertThat(created.getContent()).isEqualTo("hello");
        assertThat(created.getAuthor().getId()).isEqualTo(10L);
        assertThat(created.getTimestamp()).isNotNull();
    }

    @Test
    void updateTweet_updatesFields() {
        User author = User.builder().id(11L).email("a@b.com").handle("ab").build();
        Tweet existing = Tweet.builder().id(4L).content("old").author(author).build();
        when(tweetRepository.findByIdAndDeletedAtIsNull(4L)).thenReturn(Optional.of(existing));
        when(userRepository.findByIdAndDeletedAtIsNull(11L)).thenReturn(Optional.of(author));
        when(tweetRepository.save(existing)).thenReturn(existing);

        TweetRequest request = new TweetRequest("updated", 11L);
        Tweet updated = tweetService.updateTweet(4L, request);

        assertThat(updated.getContent()).isEqualTo("updated");
        assertThat(updated.getAuthor().getId()).isEqualTo(11L);
    }

    @Test
    void deleteTweet_setsDeletedAt() {
        Tweet existing = Tweet.builder().id(5L).content("hello").build();
        when(tweetRepository.findByIdAndDeletedAtIsNull(5L)).thenReturn(Optional.of(existing));

        tweetService.deleteTweet(5L);

        ArgumentCaptor<Tweet> captor = ArgumentCaptor.forClass(Tweet.class);
        verify(tweetRepository).save(captor.capture());
        LocalDateTime deletedAt = captor.getValue().getDeletedAt();
        assertThat(deletedAt).isNotNull();
    }
}
