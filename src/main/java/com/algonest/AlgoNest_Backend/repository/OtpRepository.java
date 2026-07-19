package com.algonest.AlgoNest_Backend.repository;

import com.algonest.AlgoNest_Backend.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OtpRepository extends JpaRepository<OtpVerification, UUID> {
    void deleteByEmail(String email);
    Optional<OtpVerification> findByEmailAndOtpAndUsedFalse(String email, String otp);
}