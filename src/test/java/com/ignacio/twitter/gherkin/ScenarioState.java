package com.ignacio.twitter.gherkin;

import com.fasterxml.jackson.databind.JsonNode;
import io.cucumber.spring.ScenarioScope;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;

@Component
@ScenarioScope
public class ScenarioState {

    private MvcResult lastResult;
    private String lastResponseBody;
    private JsonNode lastJson;
    private final Map<String, Long> userIdsByHandle = new HashMap<>();
    private Long lastTweetId;
    private Long lastTweetAuthorId;
    private String accessToken;

    public MvcResult getLastResult() {
        return lastResult;
    }

    public void setLastResult(MvcResult lastResult) {
        this.lastResult = lastResult;
    }

    public String getLastResponseBody() {
        return lastResponseBody;
    }

    public void setLastResponseBody(String lastResponseBody) {
        this.lastResponseBody = lastResponseBody;
    }

    public JsonNode getLastJson() {
        return lastJson;
    }

    public void setLastJson(JsonNode lastJson) {
        this.lastJson = lastJson;
    }

    public Map<String, Long> getUserIdsByHandle() {
        return userIdsByHandle;
    }

    public Long getLastTweetId() {
        return lastTweetId;
    }

    public void setLastTweetId(Long lastTweetId) {
        this.lastTweetId = lastTweetId;
    }

    public Long getLastTweetAuthorId() {
        return lastTweetAuthorId;
    }

    public void setLastTweetAuthorId(Long lastTweetAuthorId) {
        this.lastTweetAuthorId = lastTweetAuthorId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
