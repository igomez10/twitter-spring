package com.ignacio.twitter.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TokenResponse(
        @JsonProperty("access_token") String accessToken,
        List<String> actions
) {
}
