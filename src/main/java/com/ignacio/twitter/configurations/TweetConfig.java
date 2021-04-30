package com.ignacio.twitter.configurations;

import com.ignacio.twitter.models.Tweet;
import com.ignacio.twitter.models.User;
import com.ignacio.twitter.repositories.TweetRepository;
import com.ignacio.twitter.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class TweetConfig {
    @Bean
    CommandLineRunner tweetCommandLineRunner(TweetRepository tweetRepository, UserRepository userRepository) {
        return args -> {
            User user1 = new User("Ignacio", "Gomez", "ignacio@gomez.com", "nachogomez");
            user1.setId(1L);
            userRepository.saveAndFlush(user1);
            Tweet tweet1 = new Tweet("hello", user1, LocalDateTime.now());
            tweetRepository.saveAndFlush(tweet1);
        };
    }

}
