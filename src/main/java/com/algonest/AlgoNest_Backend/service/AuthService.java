package com.algonest.AlgoNest_Backend.service;

import com.algonest.AlgoNest_Backend.cache.PasswordResetCache;
import com.algonest.AlgoNest_Backend.cache.SignupCache;
import com.algonest.AlgoNest_Backend.client.SupabaseAdminClient;
import com.algonest.AlgoNest_Backend.dto.SignupRequest;
import com.algonest.AlgoNest_Backend.entity.User;
import com.algonest.AlgoNest_Backend.exception.EmailAlreadyExistsException;
import com.algonest.AlgoNest_Backend.exception.InvalidOtpException;
import com.algonest.AlgoNest_Backend.exception.UserNotFoundException;
import com.algonest.AlgoNest_Backend.repository.UserRepository;
import jakarta.mail.MessagingException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Service
public class AuthService {

    private final OtpService otpService;
    private final SignupCache signupCache;
    private final SupabaseAdminClient supabaseAdmin;
    private final UserRepository userRepository;
    private final ResetEmailService resetEmailService;

    public AuthService(
            OtpService otpService,
            SignupCache signupCache,
            SupabaseAdminClient supabaseAdmin,
            UserRepository userRepository,
            ResetEmailService resetEmailService
    ) {
        this.otpService = otpService;
        this.signupCache = signupCache;
        this.supabaseAdmin = supabaseAdmin;
        this.userRepository = userRepository;
        this.resetEmailService = resetEmailService;
    }

    /*
     * ==========================
     * SIGNUP
     * ==========================
     */

    public Map<String, String> signup(SignupRequest request) throws MessagingException {

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match.");
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException();
        }

        signupCache.put(request.getEmail(), request);

        otpService.generateAndSendOtp(request.getEmail());

        return Map.of(
                "message",
                "OTP sent successfully."
        );
    }

    /*
     * ==========================
     * VERIFY OTP
     * ==========================
     */

    public Map<String, String> verifySignupOtp(String email, String otp) {

        if (!otpService.verifyOtp(email, otp)) {
            throw new InvalidOtpException();
        }

        SignupRequest signup = signupCache.get(email);

        if (signup == null) {
            throw new IllegalStateException("Signup session expired.");
        }

        String authId = supabaseAdmin.createUser(
                signup.getEmail(),
                signup.getPassword(),
                signup.getName()
        );

        User user = new User();

        user.setAuthUserId(UUID.fromString(authId));
        user.setEmail(signup.getEmail());
        user.setDisplayName(signup.getName());

        userRepository.save(user);

        signupCache.remove(email);

        return Map.of(
                "message",
                "Account created successfully."
        );
    }

    /*
     * ==========================
     * FORGOT PASSWORD
     * ==========================
     */

    public Map<String, String> forgotPassword(String email) throws MessagingException {

        if (userRepository.findByEmail(email).isEmpty()) {
            throw new UserNotFoundException();
        }

        String otp = String.format("%06d", new Random().nextInt(999999));

        PasswordResetCache.put(email, otp);

        resetEmailService.sendOtp(email, otp);

        return Map.of(
                "message",
                "OTP sent successfully."
        );
    }

    /*
     * ==========================
     * VERIFY RESET OTP
     * ==========================
     */

    public Map<String, String> verifyResetOtp(
            String email,
            String otp
    ) {

        String storedOtp = PasswordResetCache.get(email);

        if (storedOtp == null || !storedOtp.equals(otp)) {
            throw new InvalidOtpException();
        }

        return Map.of(
                "message",
                "OTP verified successfully."
        );
    }

    /*
     * ==========================
     * RESET PASSWORD
     * ==========================
     */

    public Map<String, String> resetPassword(
            String email,
            String newPassword
    ) {

        String userId = supabaseAdmin.getUserIdByEmail(email);

        supabaseAdmin.updateUserPassword(
                userId,
                newPassword
        );

        PasswordResetCache.remove(email);

        return Map.of(
                "message",
                "Password updated successfully."
        );
    }

}