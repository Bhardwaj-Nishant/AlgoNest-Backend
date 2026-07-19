package com.algonest.AlgoNest_Backend.exception;

public class InvalidOtpException extends RuntimeException {

    public InvalidOtpException() {
        super("Invalid or expired OTP.");
    }

    public InvalidOtpException(String message) {
        super(message);
    }

}