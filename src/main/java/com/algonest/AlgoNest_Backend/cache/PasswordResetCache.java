package com.algonest.AlgoNest_Backend.cache;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PasswordResetCache {
    private static final Map<String, String> emailCache = new ConcurrentHashMap<>();

    public static String get(String email) {
        return emailCache.get(email);
    }

    public static void remove(String email) {
        emailCache.remove(email);
    }

    public static void put(String email, String otp) { emailCache.put(email, otp); }
    public String getAndRemove(String email) { return emailCache.remove(email); }
}