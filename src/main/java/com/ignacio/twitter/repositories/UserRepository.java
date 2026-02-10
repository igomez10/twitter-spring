package com.ignacio.twitter.repositories;

import com.ignacio.twitter.models.User;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {

    private final EntityManager entityManager;

    public UserRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public List<User> findAllByDeletedAtIsNull() {
        return entityManager.createQuery(
                        "select u from users u where u.deletedAt is null",
                        User.class)
                .getResultList();
    }

    @Transactional(readOnly = true)
    public Optional<User> findByIdAndDeletedAtIsNull(Long id) {
        List<User> results = entityManager.createQuery(
                        "select u from users u where u.id = :id and u.deletedAt is null",
                        User.class)
                .setParameter("id", id)
                .getResultList();
        if (results.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(results.get(0));
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(entityManager.find(User.class, id));
    }

    @Transactional(readOnly = true)
    public Optional<User> findByHandle(String handle) {
        List<User> results = entityManager.createQuery(
                        "select u from users u where u.handle = :handle",
                        User.class)
                .setParameter("handle", handle)
                .getResultList();
        if (results.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(results.get(0));
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        List<User> results = entityManager.createQuery(
                        "select u from users u where u.email = :email",
                        User.class)
                .setParameter("email", email)
                .getResultList();
        if (results.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(results.get(0));
    }

    @Transactional
    public User save(User user) {
        if (user.getId() == null) {
            entityManager.persist(user);
            return user;
        }
        if (entityManager.contains(user)) {
            return user;
        }
        return entityManager.merge(user);
    }

    @Transactional
    public void deleteAll() {
        entityManager.createNativeQuery("delete from user_to_roles").executeUpdate();
        entityManager.createNativeQuery("delete from user_credentials").executeUpdate();
        entityManager.createQuery("delete from users").executeUpdate();
    }
}
