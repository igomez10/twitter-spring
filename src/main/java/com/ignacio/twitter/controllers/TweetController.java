package com.ignacio.twitter.controllers;

import com.ignacio.twitter.repositories.TweetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class TweetController {

    @Autowired
    private TweetRepository repository;

    public TweetController(TweetRepository repository) {
        this.repository = repository;
    }
}
