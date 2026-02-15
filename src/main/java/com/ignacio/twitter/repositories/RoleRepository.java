package com.ignacio.twitter.repositories;

import com.ignacio.twitter.models.Role;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class RoleRepository {

    private final EntityManager entityManager;

    public RoleRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public Optional<Role> findByName(String name) {
        List<Role> results = entityManager.createQuery(
                        "select r from roles r where r.name = :name",
                        Role.class)
                .setParameter("name", name)
                .getResultList();
        if (results.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(results.get(0));
    }
}
