package com.ignacio.twitter.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity(name = "tweets")
public class Tweet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 200, nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User author;

    private LocalDateTime timestamp;

    public Long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Tweet(Long id, String content, User author, LocalDateTime timestamp) {
        this.id = id;
        this.content = content;
        this.author = author;
        this.timestamp = timestamp;
    }

    public Tweet(String content, User author, LocalDateTime timestamp) {
        this.content = content;
        this.author = author;
        this.timestamp = timestamp;
    }

    public Tweet(String content, User author) {
        this.content = content;
        this.author = author;
    }

    public Tweet() {

    }
}
