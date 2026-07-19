package com.algonest.AlgoNest_Backend.cache;

import com.algonest.AlgoNest_Backend.dto.SignupRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SignupCache {

    private final Map<String, SignupRequest> cache = new ConcurrentHashMap<>();

    public void put(String email, SignupRequest request) {
        cache.put(email, request);
    }

    public SignupRequest getAndRemove(String email) {
        return cache.remove(email);
    }

    @Scheduled(fixedRate = 300000) // 5 minutes
    public void cleanUp() {
        cache.clear();
    }

    public SignupRequest get(String email) {
        return cache.get(email);
    }

    public void remove(String email) {
        cache.remove(email);
    }
}