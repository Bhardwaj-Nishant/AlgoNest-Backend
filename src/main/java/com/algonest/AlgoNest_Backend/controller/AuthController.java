package com.algonest.AlgoNest_Backend.controller;

import com.algonest.AlgoNest_Backend.dto.*;
import com.algonest.AlgoNest_Backend.service.AuthService;
import jakarta.mail.MessagingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /*
     * ==========================
     * SIGNUP
     * ==========================
     */

    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signup(
            @RequestBody SignupRequest request
    ) throws MessagingException {

        return ResponseEntity.ok(
                authService.signup(request)
        );
    }

    /*
     * ==========================
     * VERIFY SIGNUP OTP
     * ==========================
     */

    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, String>> verifyOtp(
            @RequestBody VerifyOtpRequest request
    ) {

        return ResponseEntity.ok(
                authService.verifySignupOtp(
                        request.getEmail(),
                        request.getOtp()
                )
        );
    }

    /*
     * ==========================
     * FORGOT PASSWORD
     * ==========================
     */

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid
            @RequestBody ForgotPasswordRequest request
    ) throws MessagingException {

        return ResponseEntity.ok(
                authService.forgotPassword(request.getEmail())
        );
    }

    /*
     * ==========================
     * VERIFY RESET OTP
     * ==========================
     */

    @PostMapping("/verify-reset-otp")
    public ResponseEntity<Map<String, String>> verifyResetOtp(
            @Valid
            @RequestBody VerifyResetOtpRequest request
    ) {

        return ResponseEntity.ok(
                authService.verifyResetOtp(
                        request.getEmail(),
                        request.getOtp()
                )
        );
    }

    /*
     * ==========================
     * RESET PASSWORD
     * ==========================
     */

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid
            @RequestBody ResetPasswordRequest request
    ) {

        return ResponseEntity.ok(
                authService.resetPassword(
                        request.getEmail(),
                        request.getNewPassword()
                )
        );
    }

}