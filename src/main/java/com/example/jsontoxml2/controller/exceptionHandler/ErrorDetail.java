package com.example.jsontoxml2.controller.exceptionHandler;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorDetail {
    private final String fieldName;
    private final Object invalidValue;
    private final String constraint;

}
