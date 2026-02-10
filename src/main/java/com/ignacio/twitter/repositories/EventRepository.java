package com.ignacio.twitter.repositories;

import com.ignacio.twitter.models.Event;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class EventRepository {

    private final EntityManager entityManager;

    public EventRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional
    public Event save(Event event) {
        if (event.getId() == null) {
            entityManager.persist(event);
            return event;
        }
        if (entityManager.contains(event)) {
            return event;
        }
        return entityManager.merge(event);
    }
}
