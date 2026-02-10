package com.ignacio.twitter.controllers;

import com.ignacio.twitter.dto.UserRequest;
import com.ignacio.twitter.middleware.AuthContext;
import com.ignacio.twitter.models.User;
import com.ignacio.twitter.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public List<User> listUsers() {
        return userService.listUsers();
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.getUser(id);
    }

    @PostMapping
    public User createUser(@Valid @RequestBody UserRequest request, HttpServletRequest httpRequest) {
        Long actorUserId = AuthContext.getUserId(httpRequest).orElse(null);
        return userService.createUser(request, actorUserId);
    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @Valid @RequestBody UserRequest request,
                           HttpServletRequest httpRequest) {
        Long actorUserId = AuthContext.getUserId(httpRequest).orElse(null);
        return userService.updateUser(id, request, actorUserId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long actorUserId = AuthContext.getUserId(httpRequest).orElse(null);
        userService.deleteUser(id, actorUserId);
        return ResponseEntity.noContent().build();
    }
}
