package com.ignacio.twitter.repositories;

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
@Import(UserRepository.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void findAllByDeletedAtIsNull_filtersDeletedUsers() {
        User active = persistUser("active@example.com", "active", null);
        persistUser("deleted@example.com", "deleted", LocalDateTime.now());

        entityManager.flush();
        entityManager.clear();

        List<User> results = userRepository.findAllByDeletedAtIsNull();

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo(active.getId());
    }

    @Test
    void findByIdAndDeletedAtIsNull_returnsEmptyForDeleted() {
        User active = persistUser("active2@example.com", "active2", null);
        User deleted = persistUser("deleted2@example.com", "deleted2", LocalDateTime.now());

        entityManager.flush();
        entityManager.clear();

        assertThat(userRepository.findByIdAndDeletedAtIsNull(active.getId())).isPresent();
        assertThat(userRepository.findByIdAndDeletedAtIsNull(deleted.getId())).isEmpty();
    }

    @Test
    void save_persistsAndUpdates() {
        User user = User.builder()
                .firstName("First")
                .lastName("Last")
                .email("save@example.com")
                .handle("savehandle")
                .build();

        User saved = userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        User stored = entityManager.find(User.class, saved.getId());
        assertThat(stored).isNotNull();
        assertThat(stored.getEmail()).isEqualTo("save@example.com");

        User detached = User.builder()
                .id(saved.getId())
                .firstName("Updated")
                .lastName("Name")
                .email("updated@example.com")
                .handle("updatedhandle")
                .build();

        userRepository.save(detached);
        entityManager.flush();
        entityManager.clear();

        User updated = entityManager.find(User.class, saved.getId());
        assertThat(updated.getEmail()).isEqualTo("updated@example.com");
        assertThat(updated.getHandle()).isEqualTo("updatedhandle");
    }

    @Test
    void deleteAll_removesRows() {
        persistUser("one@example.com", "one", null);
        persistUser("two@example.com", "two", null);
        entityManager.flush();

        userRepository.deleteAll();
        entityManager.flush();

        Long count = entityManager.createQuery("select count(u) from users u", Long.class)
                .getSingleResult();

        assertThat(count).isZero();
    }

    private User persistUser(String email, String handle, LocalDateTime deletedAt) {
        User user = User.builder()
                .firstName("First")
                .lastName("Last")
                .email(email)
                .handle(handle)
                .deletedAt(deletedAt)
                .build();
        entityManager.persist(user);
        return user;
    }
}
