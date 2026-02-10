package com.ignacio.twitter.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ignacio.twitter.dto.TokenRequest;
import com.ignacio.twitter.dto.UserRequest;
import com.ignacio.twitter.models.PermittedAction;
import com.ignacio.twitter.models.Role;
import com.ignacio.twitter.models.Scope;
import com.ignacio.twitter.models.User;
import com.ignacio.twitter.services.UserService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OAuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        entityManager.createNativeQuery("delete from user_to_roles").executeUpdate();
        entityManager.createNativeQuery("delete from roles_to_scopes").executeUpdate();
        entityManager.createNativeQuery("delete from scopes_to_permitted_actions").executeUpdate();
        entityManager.createNativeQuery("delete from permitted_actions").executeUpdate();
        entityManager.createNativeQuery("delete from scopes").executeUpdate();
        entityManager.createNativeQuery("delete from roles").executeUpdate();
    }

    @Test
    void tokenIncludesPermittedActions() throws Exception {
        UserRequest userRequest = new UserRequest(
                "Ada",
                "Lovelace",
                "ada@lovelace.com",
                "adal",
                "adal",
                "password123"
        );
        User user = userService.createUser(userRequest);

        PermittedAction action = PermittedAction.builder().action("tweet:read").build();
        Scope scope = Scope.builder().name("tweets").build();
        scope.getPermittedActions().add(action);
        Role role = Role.builder().name("basic").build();
        role.getScopes().add(scope);

        entityManager.persist(action);
        entityManager.persist(scope);
        entityManager.persist(role);

        User managedUser = entityManager.find(User.class, user.getId());
        managedUser.getRoles().add(role);
        entityManager.flush();
        entityManager.clear();

        TokenRequest tokenRequest = new TokenRequest("adal", "password123");

        mockMvc.perform(post("/oauth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tokenRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").exists())
                .andExpect(jsonPath("$.actions[0]").value("tweet:read"));
    }

    @Test
    void tokenRejectsInvalidCredentials() throws Exception {
        UserRequest userRequest = new UserRequest(
                "Alan",
                "Turing",
                "alan@turing.com",
                "aturing",
                "aturing",
                "password123"
        );
        userService.createUser(userRequest);

        TokenRequest tokenRequest = new TokenRequest("aturing", "wrongpassword");

        mockMvc.perform(post("/oauth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tokenRequest)))
                .andExpect(status().isUnauthorized());
    }
}
