package com.algonest.AlgoNest_Backend.util;

import com.algonest.AlgoNest_Backend.entity.User;
import com.algonest.AlgoNest_Backend.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AuthUtil {

    private final UserRepository userRepository;

    public AuthUtil(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UUID getCurrentUserId() {
        return (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public User getCurrentUser() {
        UUID userId = getCurrentUserId();
        return userRepository.findByAuthUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}