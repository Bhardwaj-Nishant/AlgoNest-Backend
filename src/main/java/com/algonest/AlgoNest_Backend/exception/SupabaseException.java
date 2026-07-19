package com.algonest.AlgoNest_Backend.exception;

public class SupabaseException extends RuntimeException {

    public SupabaseException() {
        super("Supabase request failed.");
    }

    public SupabaseException(String message) {
        super(message);
    }

    public SupabaseException(String message, Throwable cause) {
        super(message, cause);
    }

}