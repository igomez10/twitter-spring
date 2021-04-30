package com.ignacio.twitter.models;

import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity(name = "tweets")
public class Tweet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ID;

    @Column(length = 200, nullable = false)
    private String Content;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User Author;

    private LocalDateTime Timestamp;

    @CreatedDate
    public Long getID() {
        return ID;
    }

    public String getContent() {
        return Content;
    }

    public void setContent(String content) {
        Content = content;
    }

    public User getAuthor() {
        return Author;
    }

    public void setAuthor(User author) {
        Author = author;
    }

    public LocalDateTime getTimestamp() {
        return Timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        Timestamp = timestamp;
    }

    public Tweet(Long ID, String content, User author, LocalDateTime timestamp) {
        this.ID = ID;
        Content = content;
        Author = author;
        Timestamp = timestamp;
    }

    public Tweet(String content, User author, LocalDateTime timestamp) {
        Content = content;
        Author = author;
        Timestamp = timestamp;
    }

    public Tweet(String content, User author) {
        Content = content;
        Author = author;
    }

    public Tweet() {

    }
}
