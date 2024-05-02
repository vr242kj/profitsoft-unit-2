package com.example.jsontoxml2.model.dto.post;

import com.example.jsontoxml2.model.dto.user.UserIdDto;
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
public class PostUpdateRequestDto {

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must be at most 100 characters")
    private String title;

    @NotBlank(message = "Text is required")
    @Size(max = 500, message = "Text must be at most 500 characters")
    private String content;

    @NotNull
    private Boolean isPublished;

    @NotNull
    private Integer likesCount;

    @NotNull
    private UserIdDto user;

}
