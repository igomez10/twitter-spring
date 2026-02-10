package com.ignacio.twitter.auth;

import java.util.List;

public record AuthenticatedUser(Long userId, List<String> actions) {
}
