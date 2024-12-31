package com.auth.exception;

public class AccountActivationException extends RuntimeException {
    public AccountActivationException(String message) {
        super(message);
    }
}