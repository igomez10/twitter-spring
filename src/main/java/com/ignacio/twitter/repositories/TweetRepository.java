package com.ignacio.twitter.repositories;

import com.ignacio.twitter.models.Tweet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TweetRepository extends JpaRepository<Tweet, UUID> {
}
