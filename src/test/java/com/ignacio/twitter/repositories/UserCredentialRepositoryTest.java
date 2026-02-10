package com.ignacio.twitter.repositories;

import com.ignacio.twitter.models.User;
import com.ignacio.twitter.models.UserCredential;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(UserCredentialRepository.class)
class UserCredentialRepositoryTest {

    @Autowired
    private UserCredentialRepository userCredentialRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void saveAndFindByUsername() {
        User user = User.builder()
                .firstName("Ada")
                .lastName("Lovelace")
                .email("ada@lovelace.com")
                .handle("adal")
                .build();
        entityManager.persist(user);
        entityManager.flush();

        UserCredential credential = UserCredential.builder()
                .userId(user.getId())
                .username("adal")
                .passwordHash("$2a$10$abcdefghijklmnopqrstuvwx1234567890abcd")
                .passwordSalt("$2a$10$abcdefghijklmnopqrstuvwx")
                .build();
        userCredentialRepository.save(credential);
        entityManager.flush();
        entityManager.clear();

        Optional<UserCredential> found = userCredentialRepository.findByUsername("adal");

        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(user.getId());
        assertThat(found.get().getUsername()).isEqualTo("adal");
    }
}
