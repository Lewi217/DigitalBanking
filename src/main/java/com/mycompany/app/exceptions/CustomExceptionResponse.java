package com.mycompany.app.exceptions;

public class CustomExceptionResponse extends RuntimeException {
    public CustomExceptionResponse(String message) {
        super(message);
    }
}
