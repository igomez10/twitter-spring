package com.ignacio.twitter.controllers;

import com.ignacio.twitter.models.User;
import com.ignacio.twitter.repositories.TweetRepository;
import com.ignacio.twitter.repositories.UserRepository;
import com.ignacio.twitter.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController()
public class UserController {

    @Autowired
    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public List<User> ListUsers() {
        return userService.listUsers();
    }
}
