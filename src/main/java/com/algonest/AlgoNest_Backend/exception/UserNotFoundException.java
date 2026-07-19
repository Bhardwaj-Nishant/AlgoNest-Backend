package com.algonest.AlgoNest_Backend.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException() {
        super("User not found.");
    }

    public UserNotFoundException(String message) {
        super(message);
    }

}