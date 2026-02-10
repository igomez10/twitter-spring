package com.ignacio.twitter.repositories;

import com.ignacio.twitter.models.Tweet;
import com.ignacio.twitter.models.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TweetRepository.class)
class TweetRepositoryTest {

    @Autowired
    private TweetRepository tweetRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void findAllByDeletedAtIsNull_filtersDeletedTweets() {
        User author = persistUser("author@example.com", "author");
        Tweet active = persistTweet(author, "active", null);
        persistTweet(author, "deleted", LocalDateTime.now());

        entityManager.flush();
        entityManager.clear();

        List<Tweet> results = tweetRepository.findAllByDeletedAtIsNull();

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo(active.getId());
    }

    @Test
    void findByIdAndDeletedAtIsNull_returnsEmptyForDeleted() {
        User author = persistUser("author2@example.com", "author2");
        Tweet active = persistTweet(author, "active", null);
        Tweet deleted = persistTweet(author, "deleted", LocalDateTime.now());

        entityManager.flush();
        entityManager.clear();

        assertThat(tweetRepository.findByIdAndDeletedAtIsNull(active.getId())).isPresent();
        assertThat(tweetRepository.findByIdAndDeletedAtIsNull(deleted.getId())).isEmpty();
    }

    @Test
    void save_persistsAndUpdates() {
        User author = persistUser("save@author.com", "saveauthor");
        entityManager.flush();
        entityManager.clear();

        User managedAuthor = entityManager.find(User.class, author.getId());
        Tweet tweet = Tweet.builder()
                .content("hello")
                .author(managedAuthor)
                .build();

        Tweet saved = tweetRepository.save(tweet);
        entityManager.flush();
        entityManager.clear();

        Tweet stored = entityManager.find(Tweet.class, saved.getId());
        assertThat(stored.getContent()).isEqualTo("hello");

        User updatedAuthor = entityManager.find(User.class, author.getId());
        Tweet detached = Tweet.builder()
                .id(saved.getId())
                .content("updated")
                .author(updatedAuthor)
                .build();

        tweetRepository.save(detached);
        entityManager.flush();
        entityManager.clear();

        Tweet updated = entityManager.find(Tweet.class, saved.getId());
        assertThat(updated.getContent()).isEqualTo("updated");
    }

    @Test
    void deleteAll_removesRows() {
        User author = persistUser("delete@author.com", "deleteauthor");
        persistTweet(author, "one", null);
        persistTweet(author, "two", null);
        entityManager.flush();

        tweetRepository.deleteAll();
        entityManager.flush();

        Long count = entityManager.createQuery("select count(t) from tweets t", Long.class)
                .getSingleResult();

        assertThat(count).isZero();
    }

    private User persistUser(String email, String handle) {
        User user = User.builder()
                .firstName("First")
                .lastName("Last")
                .email(email)
                .handle(handle)
                .build();
        entityManager.persist(user);
        return user;
    }

    private Tweet persistTweet(User author, String content, LocalDateTime deletedAt) {
        Tweet tweet = Tweet.builder()
                .content(content)
                .author(author)
                .deletedAt(deletedAt)
                .build();
        entityManager.persist(tweet);
        return tweet;
    }
}
