package com.algonest.AlgoNest_Backend.service;

import com.algonest.AlgoNest_Backend.entity.OtpVerification;
import com.algonest.AlgoNest_Backend.repository.OtpRepository;
import jakarta.mail.MessagingException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Random;

@Service
public class OtpService {

    private final OtpRepository otpRepository;
    private final EmailService emailService;
    private static final SecureRandom RANDOM = new SecureRandom();

    public OtpService(OtpRepository otpRepository, EmailService emailService) {
        this.otpRepository = otpRepository;
        this.emailService = emailService;
    }


    @Transactional
    public String generateAndSendOtp(String email) throws MessagingException {

        otpRepository.deleteByEmail(email);

        String otp = String.format("%06d", RANDOM.nextInt(1_000_000));

        OtpVerification entity = new OtpVerification();
        entity.setEmail(email);
        entity.setOtp(otp);
        entity.setExpiry(LocalDateTime.now().plusMinutes(5));

        otpRepository.save(entity);

        emailService.sendOtp(email, otp);

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