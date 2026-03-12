package com.adoption.petadoptionserver.exception;

/**
 * Custom exception thrown when authentication fails.
 */
public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String message) {
        super(message);
    }
}