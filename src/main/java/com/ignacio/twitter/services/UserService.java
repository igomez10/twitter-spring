package com.ignacio.twitter.services;

import com.ignacio.twitter.dto.UserRequest;
import com.ignacio.twitter.models.User;

import java.util.List;

public interface UserService {
    List<User> listUsers();

    User getUser(Long id);

    User createUser(UserRequest request, Long actorUserId);

    User updateUser(Long id, UserRequest request, Long actorUserId);

    void deleteUser(Long id, Long actorUserId);
}
