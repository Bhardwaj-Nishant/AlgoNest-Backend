package com.algonest.AlgoNest_Backend.repository;

import com.algonest.AlgoNest_Backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByAuthUserId(UUID authUserId);

    Optional<User> findByEmail(String email);

}