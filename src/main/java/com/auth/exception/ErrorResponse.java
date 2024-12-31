package com.auth.exception;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
public class ErrorResponse {
    private String details;
    private Integer statusCode;
    private String message;
}