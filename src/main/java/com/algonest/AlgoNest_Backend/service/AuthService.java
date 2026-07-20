package com.algonest.AlgoNest_Backend.service;

import com.algonest.AlgoNest_Backend.cache.PasswordResetCache;
import com.algonest.AlgoNest_Backend.cache.SignupCache;
import com.algonest.AlgoNest_Backend.client.SupabaseAdminClient;
import com.algonest.AlgoNest_Backend.dto.SignupRequest;
import com.algonest.AlgoNest_Backend.entity.User;
import com.algonest.AlgoNest_Backend.repository.UserRepository;
import jakarta.mail.MessagingException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {

    private final OtpService otpService;
    private final SignupCache signupCache;
    private final PasswordResetCache passwordResetCache;
    private final SupabaseAdminClient supabaseAdminClient;
    private final UserRepository userRepository;
    // ✅ No direct email service field needed – OtpService handles it

    public AuthService(OtpService otpService,
                       SignupCache signupCache,
                       PasswordResetCache passwordResetCache,
                       SupabaseAdminClient supabaseAdminClient,
                       UserRepository userRepository) {
        this.otpService = otpService;
        this.signupCache = signupCache;
        this.passwordResetCache = passwordResetCache;
        this.supabaseAdminClient = supabaseAdminClient;
        this.userRepository = userRepository;
    }

    // ==========================
    // SIGNUP
    // ==========================

    @Transactional
    public Map<String, String> signup(SignupRequest request) throws MessagingException {
        Map<String, String> response = new HashMap<>();

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            response.put("message", "Email already registered. Please login.");
            return response;
        }

        boolean existsInSupabase = supabaseAdminClient.emailExists(request.getEmail());
        if (existsInSupabase) {
            response.put("message", "Email already registered. Please login.");
            return response;
        }

        // ✅ This method now generates AND sends the OTP (via SendGrid)
        String otp = otpService.generateAndSendOtp(request.getEmail(), "signup");
        signupCache.put(request.getEmail(), request);

        // ❌ REMOVED duplicate emailService.sendOtp call

        response.put("message", "OTP sent to your email.");
        return response;
    }

    // ==========================
    // VERIFY SIGNUP OTP
    // ==========================

    @Transactional
    public Map<String, String> verifySignupOtp(String email, String otp) {
        Map<String, String> response = new HashMap<>();

        if (!otpService.verifyOtp(email, otp)) {
            response.put("message", "Invalid or expired OTP. Please request a new one.");
            return response;
        }

        SignupRequest signupData = signupCache.getAndRemove(email);
        if (signupData == null) {
            response.put("message", "Your signup session has expired. Please try again.");
            return response;
        }

        String supabaseUserId;
        try {
            supabaseUserId = supabaseAdminClient.createUser(
                    signupData.getEmail(),
                    signupData.getPassword(),
                    signupData.getName()
            );
        } catch (RuntimeException e) {
            if (e.getMessage().contains("email_exists") || e.getMessage().contains("already registered")) {
                response.put("message", "This email is already registered. Please login.");
            } else {
                response.put("message", "Account creation failed: " + e.getMessage());
            }
            return response;
        }

        User user = new User();
        user.setAuthUserId(UUID.fromString(supabaseUserId));
        user.setEmail(signupData.getEmail());
        user.setDisplayName(signupData.getName());
        userRepository.save(user);

        response.put("message", "Account created successfully! Please log in.");
        return response;
    }

    // ==========================
    // FORGOT PASSWORD
    // ==========================

    @Transactional
    public Map<String, String> forgotPassword(String email) throws MessagingException {
        Map<String, String> response = new HashMap<>();

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            boolean existsInSupabase = supabaseAdminClient.emailExists(email);
            if (!existsInSupabase) {
                response.put("message", "No account found with this email.");
                return response;
            }
        }

        // ✅ This method now generates AND sends the reset OTP (via SendGrid)
        String otp = otpService.generateAndSendOtp(email, "reset");
        passwordResetCache.put(email, otp);

        // ❌ REMOVED duplicate emailService.sendOtp call

        response.put("message", "OTP sent to your email.");
        return response;
    }

    // ==========================
    // VERIFY RESET OTP
    // ==========================

    public Map<String, String> verifyResetOtp(String email, String otp) {
        Map<String, String> response = new HashMap<>();

        String storedOtp = passwordResetCache.getAndRemove(email);
        if (storedOtp == null || !storedOtp.equals(otp)) {
            response.put("message", "Invalid or expired OTP.");
            return response;
        }

        response.put("message", "OTP verified successfully.");
        return response;
    }

    // ==========================
    // RESET PASSWORD
    // ==========================

    @Transactional
    public Map<String, String> resetPassword(String email, String newPassword) {
        Map<String, String> response = new HashMap<>();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            supabaseAdminClient.updateUserPassword(user.getAuthUserId().toString(), newPassword);
        } catch (RuntimeException e) {
            response.put("message", "Failed to reset password: " + e.getMessage());
            return response;
        }

        response.put("message", "Password updated successfully. Please log in.");
        return response;
    }
}