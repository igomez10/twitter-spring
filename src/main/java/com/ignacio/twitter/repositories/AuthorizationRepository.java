package com.ignacio.twitter.repositories;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class AuthorizationRepository {

    private final EntityManager entityManager;

    public AuthorizationRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public List<String> findPermittedActionsByUserId(Long userId) {
        @SuppressWarnings("unchecked")
        List<Object> results = entityManager.createNativeQuery("""
                select distinct pa.action
                from permitted_actions pa
                join scopes_to_permitted_actions stpa on stpa.permitted_action_id = pa.id
                join roles_to_scopes rts on rts.scope_id = stpa.scope_id
                join user_to_roles utr on utr.role_id = rts.role_id
                where utr.user_id = :userId
                order by pa.action
                """)
                .setParameter("userId", userId)
                .getResultList();
        return results.stream()
                .map(Object::toString)
                .collect(Collectors.toList());
    }
}
