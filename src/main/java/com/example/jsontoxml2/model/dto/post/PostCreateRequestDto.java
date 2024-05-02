package com.example.jsontoxml2.model.dto.post;

import com.example.jsontoxml2.model.dto.user.UserIdDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostCreateRequestDto {

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must be at most 100 characters")
    @JsonProperty("title")
    private String title;

    @NotBlank(message = "Text is required")
    @Size(max = 500, message = "Text must be at most 500 characters")
    @JsonProperty("content")
    private String content;

    @NotNull
    @JsonProperty("isPublished")
    private Boolean isPublished;

    @NotNull
    @JsonProperty("user")
    private UserIdDto user;

}
