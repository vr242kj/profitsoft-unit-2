package com.example.jsontoxml2.model.dto.post;

import com.example.jsontoxml2.model.dto.user.UserInfoDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostInfoDto {

    private Long id;
    private String title;
    private String content;
    private Boolean isPublished;
    private Integer likesCount;
    private UserInfoDto user;

}
