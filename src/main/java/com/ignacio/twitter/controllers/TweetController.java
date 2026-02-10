package com.ignacio.twitter.controllers;

import com.ignacio.twitter.repositories.TweetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TweetController {

    private final TweetRepository repository;
}
