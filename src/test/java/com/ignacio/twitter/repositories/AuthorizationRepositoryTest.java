package com.ignacio.twitter.repositories;

import com.ignacio.twitter.models.PermittedAction;
import com.ignacio.twitter.models.Role;
import com.ignacio.twitter.models.Scope;
import com.ignacio.twitter.models.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(AuthorizationRepository.class)
class AuthorizationRepositoryTest {

    @Autowired
    private AuthorizationRepository authorizationRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void findPermittedActionsByUserId_returnsActions() {
        PermittedAction read = PermittedAction.builder().action("tweet:read").build();
        PermittedAction write = PermittedAction.builder().action("tweet:write").build();
        entityManager.persist(read);
        entityManager.persist(write);

        Scope scope = Scope.builder().name("tweets").build();
        entityManager.persist(scope);

        Role role = Role.builder().name("basic").build();
        entityManager.persist(role);

        User user = User.builder()
                .firstName("Grace")
                .lastName("Hopper")
                .email("grace@hopper.com")
                .handle("grace")
                .build();
        entityManager.persist(user);
        entityManager.flush();

        Scope managedScope = entityManager.find(Scope.class, scope.getId());
        managedScope.getPermittedActions().add(read);
        managedScope.getPermittedActions().add(write);

        Role managedRole = entityManager.find(Role.class, role.getId());
        managedRole.getScopes().add(managedScope);

        User managedUser = entityManager.find(User.class, user.getId());
        managedUser.getRoles().add(managedRole);
        entityManager.flush();
        entityManager.clear();

        List<String> actions = authorizationRepository.findPermittedActionsByUserId(user.getId());

        assertThat(actions).containsExactly("tweet:read", "tweet:write");
    }
}
