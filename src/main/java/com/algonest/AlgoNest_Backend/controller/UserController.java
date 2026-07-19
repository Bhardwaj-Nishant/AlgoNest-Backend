package com.algonest.AlgoNest_Backend.controller;

import com.algonest.AlgoNest_Backend.client.SupabaseAdminClient;
import com.algonest.AlgoNest_Backend.entity.User;
import com.algonest.AlgoNest_Backend.repository.UserRepository;
import com.algonest.AlgoNest_Backend.util.AuthUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final AuthUtil authUtil;
    private final UserRepository userRepository;
    private final SupabaseAdminClient supabaseAdminClient;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.anon-key}")
    private String supabaseAnonKey;

    public UserController(AuthUtil authUtil, UserRepository userRepository, SupabaseAdminClient supabaseAdminClient) {
        this.authUtil = authUtil;
        this.userRepository = userRepository;
        this.supabaseAdminClient = supabaseAdminClient;
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        User user = authUtil.getCurrentUser();
        return ResponseEntity.ok(user);
    }

    @PutMapping("/me")
    public ResponseEntity<User> updateUser(@RequestBody Map<String, String> payload) {
        User user = authUtil.getCurrentUser();
        String newName = payload.get("displayName");
        if (newName != null && !newName.trim().isEmpty()) {
            user.setDisplayName(newName.trim());
            userRepository.save(user);
        }
        return ResponseEntity.ok(user);
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> payload) {
        String currentPassword = payload.get("currentPassword");
        String newPassword = payload.get("newPassword");
        if (newPassword == null || newPassword.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "New password is required."));
        }
        if (currentPassword == null || currentPassword.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Current password is required."));
        }

        User user = authUtil.getCurrentUser();

        // 1. Verify current password
        try {
            verifyCurrentPassword(user.getEmail(), currentPassword);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }

        // 2. Update password
        try {
            supabaseAdminClient.updateUserPassword(user.getAuthUserId().toString(), newPassword);
            return ResponseEntity.ok(Map.of("message", "Password updated successfully."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to update password: " + e.getMessage()));
        }
    }

    private void verifyCurrentPassword(String email, String currentPassword) {
        String url = supabaseUrl + "/auth/v1/token?grant_type=password";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", supabaseAnonKey);
        String payload = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, currentPassword);
        HttpEntity<String> entity = new HttpEntity<>(payload, headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Current password is incorrect.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Current password is incorrect.");
        }
    }

    @DeleteMapping("/me")
    @Transactional
    public ResponseEntity<?> deleteAccount() {
        User user = authUtil.getCurrentUser();
        try {
            supabaseAdminClient.deleteUser(user.getAuthUserId().toString());
            userRepository.delete(user);
            return ResponseEntity.ok(Map.of("message", "Account deleted successfully."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to delete account: " + e.getMessage()));
        }
    }
}