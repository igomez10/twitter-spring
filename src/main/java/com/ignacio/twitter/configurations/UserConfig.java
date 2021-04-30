package com.ignacio.twitter.configurations;

import com.ignacio.twitter.models.User;
import com.ignacio.twitter.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class UserConfig {
    @Bean
    CommandLineRunner userCommandLineRunner(UserRepository repository) {
        return args -> {
        };
    }

}
