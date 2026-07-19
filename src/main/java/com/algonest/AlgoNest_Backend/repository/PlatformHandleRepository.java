package com.algonest.AlgoNest_Backend.repository;

import com.algonest.AlgoNest_Backend.entity.PlatformHandle;
import com.algonest.AlgoNest_Backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlatformHandleRepository extends JpaRepository<PlatformHandle, UUID> {
    List<PlatformHandle> findByUser(User user);


    Optional<PlatformHandle> findByUserAndPlatform(User user, String platform);

}