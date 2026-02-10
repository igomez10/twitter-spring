package com.ignacio.twitter.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequest(
        @Size(max = 255) String firstName,
        @Size(max = 255) String lastName,
        @Email @NotBlank String email,
        @NotBlank String handle,
        @NotBlank @Size(max = 255) String username,
        @NotBlank @Size(max = 255) String password
) {
}
