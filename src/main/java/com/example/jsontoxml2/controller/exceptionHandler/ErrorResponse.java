package com.example.jsontoxml2.controller.exceptionHandler;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final LocalDateTime dateTime;
    private List<ErrorDetail> errors;
    private String errorMessage;

    public ErrorResponse(LocalDateTime dateTime, List<ErrorDetail> errors) {
        this.dateTime = dateTime;
        this.errors = errors;
    }

    public ErrorResponse(LocalDateTime dateTime, String errorMessage) {
        this.dateTime = dateTime;
        this.errorMessage = errorMessage;
    }

}
