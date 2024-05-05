package com.example.jsontoxml2.model.dto.post;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostQueryDto {

    @NotNull
    @JsonProperty("userId")
    private Long userId;

    @JsonProperty("isPublished")
    private Boolean isPublished;

    @PositiveOrZero(message = "Parameter must be a positive integer or 0")
    private Integer likesCount;

}
