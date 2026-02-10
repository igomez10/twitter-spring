package com.ignacio.twitter.gherkin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ignacio.twitter.dto.TokenRequest;
import com.ignacio.twitter.dto.TweetRequest;
import com.ignacio.twitter.dto.UserRequest;
import com.ignacio.twitter.models.PermittedAction;
import com.ignacio.twitter.models.Role;
import com.ignacio.twitter.models.Scope;
import com.ignacio.twitter.models.User;
import com.ignacio.twitter.services.UserService;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;

public class ApiStepDefinitions {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final EntityManager entityManager;
    private final PlatformTransactionManager transactionManager;
    private final ScenarioState state;

    @Autowired
    public ApiStepDefinitions(MockMvc mockMvc,
                              ObjectMapper objectMapper,
                              UserService userService,
                              EntityManager entityManager,
                              PlatformTransactionManager transactionManager,
                              ScenarioState state) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.userService = userService;
        this.entityManager = entityManager;
        this.transactionManager = transactionManager;
        this.state = state;
    }

    @Before
    public void resetState() {
        cleanDatabase();
        state.setLastResult(null);
        state.setLastResponseBody(null);
        state.setLastJson(null);
        state.getUserIdsByHandle().clear();
        state.setLastTweetId(null);
        state.setLastTweetAuthorId(null);
        state.setAccessToken(null);
    }

    @Given("the database is clean")
    public void theDatabaseIsClean() {
        cleanDatabase();
    }

    @Given("a user exists with:")
    public void aUserExistsWith(DataTable table) {
        UserRequest request = toUserRequest(table);
        User user = userService.createUser(request, null);
        state.getUserIdsByHandle().put(request.handle(), user.getId());
    }

    @Given("the user {string} has role {string} with action {string}")
    public void theUserHasRoleWithAction(String handle, String roleName, String actionName) {
        Long userId = requireUserId(handle);
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.executeWithoutResult(status -> {
            PermittedAction action = PermittedAction.builder().action(actionName).build();
            Scope scope = Scope.builder().name(roleName + "-scope").build();
            scope.getPermittedActions().add(action);
            Role role = Role.builder().name(roleName).build();
            role.getScopes().add(scope);

            entityManager.persist(action);
            entityManager.persist(scope);
            entityManager.persist(role);

            User managedUser = entityManager.find(User.class, userId);
            managedUser.getRoles().add(role);
            entityManager.flush();
        });
    }

    @When("I create a user with:")
    public void iCreateAUserWith(DataTable table) throws Exception {
        UserRequest request = toUserRequest(table);
        performJsonRequest(MockMvcRequestBuilders.post("/users"), request);
        if (state.getLastResult() != null && state.getLastResult().getResponse().getStatus() < 400) {
            JsonNode json = requireJson();
            Long id = json.path("id").asLong();
            state.getUserIdsByHandle().put(request.handle(), id);
        }
    }

    @When("I fetch the user {string}")
    public void iFetchTheUser(String handle) throws Exception {
        Long id = requireUserId(handle);
        performRequest(MockMvcRequestBuilders.get("/users/{id}", id));
    }

    @When("I list users")
    public void iListUsers() throws Exception {
        performRequest(MockMvcRequestBuilders.get("/users"));
    }

    @When("I update user {string} with:")
    public void iUpdateUserWith(String handle, DataTable table) throws Exception {
        Long id = requireUserId(handle);
        UserRequest request = toUserRequest(table);
        performJsonRequest(MockMvcRequestBuilders.put("/users/{id}", id), request);
        if (!handle.equals(request.handle())) {
            state.getUserIdsByHandle().remove(handle);
            state.getUserIdsByHandle().put(request.handle(), id);
        }
    }

    @When("I delete user {string}")
    public void iDeleteUser(String handle) throws Exception {
        Long id = requireUserId(handle);
        performRequest(MockMvcRequestBuilders.delete("/users/{id}", id));
    }

    @When("I create a tweet with content {string} for user {string}")
    public void iCreateATweetWithContentForUser(String content, String handle) throws Exception {
        Long authorId = requireUserId(handle);
        TweetRequest request = new TweetRequest(content, authorId);
        performJsonRequest(MockMvcRequestBuilders.post("/tweets"), request);
        JsonNode json = requireJson();
        state.setLastTweetId(json.path("id").asLong());
        state.setLastTweetAuthorId(authorId);
    }

    @When("I fetch the tweet")
    public void iFetchTheTweet() throws Exception {
        Long tweetId = requireTweetId();
        performRequest(MockMvcRequestBuilders.get("/tweets/{id}", tweetId));
    }

    @When("I list tweets")
    public void iListTweets() throws Exception {
        performRequest(MockMvcRequestBuilders.get("/tweets"));
    }

    @When("I update the tweet with content {string}")
    public void iUpdateTheTweetWithContent(String content) throws Exception {
        Long tweetId = requireTweetId();
        Long authorId = requireTweetAuthorId();
        TweetRequest request = new TweetRequest(content, authorId);
        performJsonRequest(MockMvcRequestBuilders.put("/tweets/{id}", tweetId), request);
    }

    @When("I delete the tweet")
    public void iDeleteTheTweet() throws Exception {
        Long tweetId = requireTweetId();
        performRequest(MockMvcRequestBuilders.delete("/tweets/{id}", tweetId));
    }

    @When("I request a token for username {string} with password {string}")
    public void iRequestATokenForUsernameWithPassword(String username, String password) throws Exception {
        TokenRequest request = new TokenRequest(username, password);
        performJsonRequest(MockMvcRequestBuilders.post("/oauth/token"), request);
        if (state.getLastJson() != null) {
            String token = state.getLastJson().path("access_token").asText(null);
            if (token != null && !token.isBlank()) {
                state.setAccessToken(token);
            }
        }
    }

    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int status) {
        MvcResult result = state.getLastResult();
        Assertions.assertThat(result).as("No response recorded").isNotNull();
        Assertions.assertThat(result.getResponse().getStatus()).isEqualTo(status);
    }

    @Then("the response should include user handle {string}")
    public void theResponseShouldIncludeUserHandle(String handle) {
        JsonNode json = requireJson();
        Assertions.assertThat(json.path("handle").asText()).isEqualTo(handle);
    }

    @Then("the response should include user email {string}")
    public void theResponseShouldIncludeUserEmail(String email) {
        JsonNode json = requireJson();
        Assertions.assertThat(json.path("email").asText()).isEqualTo(email);
    }

    @Then("the response should include {int} users")
    public void theResponseShouldIncludeUsers(int count) {
        JsonNode json = requireJson();
        Assertions.assertThat(json.isArray()).isTrue();
        Assertions.assertThat(json.size()).isEqualTo(count);
    }

    @Then("the response should include tweet content {string}")
    public void theResponseShouldIncludeTweetContent(String content) {
        JsonNode json = requireJson();
        Assertions.assertThat(json.path("content").asText()).isEqualTo(content);
    }

    @Then("the response should include {int} tweets")
    public void theResponseShouldIncludeTweets(int count) {
        JsonNode json = requireJson();
        Assertions.assertThat(json.isArray()).isTrue();
        Assertions.assertThat(json.size()).isEqualTo(count);
    }

    @Then("the response should have an access token")
    public void theResponseShouldHaveAnAccessToken() {
        JsonNode json = requireJson();
        String token = json.path("access_token").asText();
        Assertions.assertThat(token).isNotBlank();
    }

    @Then("the response should include action {string}")
    public void theResponseShouldIncludeAction(String action) {
        JsonNode json = requireJson();
        JsonNode actions = json.path("actions");
        Assertions.assertThat(actions.isArray()).isTrue();
        boolean found = false;
        for (JsonNode node : actions) {
            if (action.equals(node.asText())) {
                found = true;
                break;
            }
        }
        Assertions.assertThat(found).isTrue();
    }

    @Then("the response should include {int} actions")
    public void theResponseShouldIncludeActions(int count) {
        JsonNode json = requireJson();
        JsonNode actions = json.path("actions");
        Assertions.assertThat(actions.isArray()).isTrue();
        Assertions.assertThat(actions.size()).isEqualTo(count);
    }

    private UserRequest toUserRequest(DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("User table is empty");
        }
        Map<String, String> row = rows.get(0);
        return new UserRequest(
                row.get("firstName"),
                row.get("lastName"),
                row.get("email"),
                row.get("handle"),
                row.get("username"),
                row.get("password")
        );
    }

    private void performRequest(MockHttpServletRequestBuilder builder) throws Exception {
        if (state.getAccessToken() != null) {
            builder.header("Authorization", "Bearer " + state.getAccessToken());
        }
        MvcResult result = mockMvc.perform(builder).andReturn();
        state.setLastResult(result);
        String body = result.getResponse().getContentAsString();
        state.setLastResponseBody(body);
        if (body != null && !body.isBlank()) {
            state.setLastJson(objectMapper.readTree(body));
        } else {
            state.setLastJson(null);
        }
    }

    private void performJsonRequest(MockHttpServletRequestBuilder builder, Object payload) throws Exception {
        String json = objectMapper.writeValueAsString(payload);
        builder.contentType(MediaType.APPLICATION_JSON).content(json);
        performRequest(builder);
    }

    private JsonNode requireJson() {
        JsonNode json = state.getLastJson();
        Assertions.assertThat(json).as("Expected JSON response but none was found").isNotNull();
        return json;
    }

    private Long requireUserId(String handle) {
        Long id = state.getUserIdsByHandle().get(handle);
        if (id == null) {
            throw new IllegalStateException("Unknown user handle: " + handle);
        }
        return id;
    }

    private Long requireTweetId() {
        Long id = state.getLastTweetId();
        if (id == null) {
            throw new IllegalStateException("No tweet has been created");
        }
        return id;
    }

    private Long requireTweetAuthorId() {
        Long id = state.getLastTweetAuthorId();
        if (id == null) {
            throw new IllegalStateException("No tweet author has been recorded");
        }
        return id;
    }

    private void cleanDatabase() {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.executeWithoutResult(status -> {
            entityManager.createNativeQuery("delete from roles_to_scopes").executeUpdate();
            entityManager.createNativeQuery("delete from scopes_to_permitted_actions").executeUpdate();
            entityManager.createNativeQuery("delete from user_to_roles").executeUpdate();
            entityManager.createNativeQuery("delete from permitted_actions").executeUpdate();
            entityManager.createNativeQuery("delete from scopes").executeUpdate();
            entityManager.createNativeQuery("delete from roles").executeUpdate();
            entityManager.createNativeQuery("delete from tweets").executeUpdate();
            entityManager.createNativeQuery("delete from user_credentials").executeUpdate();
            entityManager.createNativeQuery("delete from events").executeUpdate();
            entityManager.createNativeQuery("delete from users").executeUpdate();
        });
    }
}
