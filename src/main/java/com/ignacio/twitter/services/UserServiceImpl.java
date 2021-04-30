package com.ignacio.twitter.services;

import com.ignacio.twitter.models.User;
import com.ignacio.twitter.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository repository;

    public UserServiceImpl(UserRepository repository) {
        this.repository = repository;
    }


    public List<User> listUsers() {
        return repository.findAll();
    }
}
