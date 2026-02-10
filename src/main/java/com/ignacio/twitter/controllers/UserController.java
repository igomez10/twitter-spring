package com.ignacio.twitter.controllers;

import com.ignacio.twitter.models.User;
import com.ignacio.twitter.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController()
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/users")
    public List<User> ListUsers() {
        return userService.listUsers();
    }
}
