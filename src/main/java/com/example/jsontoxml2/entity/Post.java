package com.example.jsontoxml2.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
@Table(name = "posts", indexes = @Index(columnList = "user_id"))
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "content")
    private String content;

    @Column(name = "published")
    private Boolean isPublished;

    @Column(name = "likes_count")
    private Integer likesCount;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "user_id")
    private User user;

    public Boolean getPublished() {
        return isPublished;
    }

    public void setPublished(Boolean published) {
        isPublished = published;
    }

    public Integer getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(Integer likesCount) {
        this.likesCount = likesCount;
    }

    public Post() {
    }

    public Post(String title, String content, User user, Boolean isPublished) {
        this.title = title;
        this.content = content;
        this.isPublished = isPublished;
        this.likesCount = 0;
        this.user = user;
    }

    public Post(Long id, String title, String content, boolean isPublished, int likesCount) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.isPublished = isPublished;
        this.likesCount = likesCount;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", isPublished=" + isPublished +
                ", likesCount=" + likesCount +
                ", user=" + user +
                '}';
    }
}
