package com.example.jsontoxml2.model.dto.post;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PostQueryWithPaginationDto extends PostQueryDto{

    @NotNull
    @PositiveOrZero(message = "Parameter must be a positive integer or 0")
    private Integer page;

    @NotNull
    @Positive(message = "Parameter must be a positive integer greater than 0")
    private Integer size;

}
