package com.ignacio.twitter.repositories;

import com.ignacio.twitter.models.UserCredential;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class UserCredentialRepository {

    private final EntityManager entityManager;

    public UserCredentialRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public Optional<UserCredential> findByUsername(String username) {
        List<UserCredential> results = entityManager.createQuery(
                        "select uc from user_credentials uc where uc.username = :username",
                        UserCredential.class)
                .setParameter("username", username)
                .getResultList();
        if (results.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(results.get(0));
    }

    @Transactional(readOnly = true)
    public Optional<UserCredential> findByUserId(Long userId) {
        return Optional.ofNullable(entityManager.find(UserCredential.class, userId));
    }

    @Transactional
    public UserCredential save(UserCredential credential) {
        if (credential.getUserId() == null) {
            entityManager.persist(credential);
            return credential;
        }
        if (entityManager.contains(credential)) {
            return credential;
        }
        return entityManager.merge(credential);
    }
}
