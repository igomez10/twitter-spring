package com.ignacio.twitter.services;

import com.ignacio.twitter.models.User;
import com.ignacio.twitter.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repository;


    public List<User> listUsers() {
        return repository.findAll();
    }
}
