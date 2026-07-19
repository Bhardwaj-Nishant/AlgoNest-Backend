package com.algonest.AlgoNest_Backend.service;

import com.algonest.AlgoNest_Backend.entity.User;
import com.algonest.AlgoNest_Backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Finds a user by authUserId, or creates a new one if they don't exist.
     * This is used for Google OAuth and future SSO logins.
     */
    public User findOrCreateUser(UUID authUserId, String email, String displayName) {
        return userRepository.findByAuthUserId(authUserId)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setAuthUserId(authUserId);
                    newUser.setEmail(email);
                    // Use the provided display name, or fallback to the email prefix
                    String finalDisplayName = displayName;
                    if (finalDisplayName == null || finalDisplayName.isEmpty()) {
                        finalDisplayName = email.split("@")[0];
                    }
                    newUser.setDisplayName(finalDisplayName);
                    return userRepository.save(newUser);
                });
    }
}