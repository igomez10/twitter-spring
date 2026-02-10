package com.ignacio.twitter.repositories;

import com.ignacio.twitter.models.Tweet;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class TweetRepository {

    private final EntityManager entityManager;

    public TweetRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public List<Tweet> findAllByDeletedAtIsNull() {
        return entityManager.createQuery(
                        "select t from tweets t where t.deletedAt is null",
                        Tweet.class)
                .getResultList();
    }

    @Transactional(readOnly = true)
    public Optional<Tweet> findByIdAndDeletedAtIsNull(Long id) {
        List<Tweet> results = entityManager.createQuery(
                        "select t from tweets t where t.id = :id and t.deletedAt is null",
                        Tweet.class)
                .setParameter("id", id)
                .getResultList();
        if (results.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(results.get(0));
    }

    @Transactional(readOnly = true)
    public Optional<Tweet> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Tweet.class, id));
    }

    @Transactional
    public Tweet save(Tweet tweet) {
        if (tweet.getId() == null) {
            entityManager.persist(tweet);
            return tweet;
        }
        if (entityManager.contains(tweet)) {
            return tweet;
        }
        return entityManager.merge(tweet);
    }

    @Transactional
    public void deleteAll() {
        entityManager.createQuery("delete from tweets").executeUpdate();
    }
}
