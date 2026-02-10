package com.ignacio.twitter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TweetRequest(
        @NotBlank @Size(max = 200) String content,
        @NotNull Long authorId
) {
}
