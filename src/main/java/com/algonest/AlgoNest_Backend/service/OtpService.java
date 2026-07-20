package com.algonest.AlgoNest_Backend.service;

import com.algonest.AlgoNest_Backend.entity.OtpVerification;
import com.algonest.AlgoNest_Backend.repository.OtpRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class OtpService {

    private final OtpRepository otpRepository;
    private final SendGridEmailService emailService;   // ✅ Use the new SendGrid service

    public OtpService(OtpRepository otpRepository, SendGridEmailService emailService) {
        this.otpRepository = otpRepository;
        this.emailService = emailService;
    }

    @Transactional
    public String generateAndSendOtp(String email, String type) {
        otpRepository.deleteByEmail(email);
        String otp = String.format("%06d", new Random().nextInt(999999));
        OtpVerification entity = new OtpVerification();
        entity.setEmail(email);
        entity.setOtp(otp);
        entity.setExpiry(LocalDateTime.now().plusMinutes(5));
        otpRepository.save(entity);

        // ✅ Pass the type (e.g., "signup" or "reset")
        emailService.sendOtp(email, otp, type);

        return otp;
    }

    @Transactional
    public boolean verifyOtp(String email, String otp) {
        return otpRepository.findByEmailAndOtpAndUsedFalse(email, otp)
                .filter(o -> o.getExpiry().isAfter(LocalDateTime.now()))
                .map(o -> {
                    o.setUsed(true);
                    otpRepository.save(o);
                    return true;
                })
                .orElse(false);
    }
}