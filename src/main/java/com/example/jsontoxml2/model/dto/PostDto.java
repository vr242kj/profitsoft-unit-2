package com.example.jsontoxml2.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostDto {

    private Long id;
    private String title;
    private String content;
    private Boolean isPublished;
    private Integer likesCount;
    private Long userId;

    public String toCSV() {
        return String.format("%d;%s;%s;%d;%b;%d", id, title, content, likesCount, isPublished, userId);
    }

}
