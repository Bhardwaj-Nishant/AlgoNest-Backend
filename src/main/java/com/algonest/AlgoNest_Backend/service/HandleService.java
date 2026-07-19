package com.algonest.AlgoNest_Backend.service;

import com.algonest.AlgoNest_Backend.entity.PlatformHandle;
import com.algonest.AlgoNest_Backend.entity.User;
import com.algonest.AlgoNest_Backend.repository.DailySnapshotRepository;
import com.algonest.AlgoNest_Backend.repository.PlatformHandleRepository;
import com.algonest.AlgoNest_Backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class HandleService {

    private final PlatformHandleRepository handleRepository;
    private final UserRepository userRepository;
    private final DailySnapshotRepository dailySnapshotRepository;

    public HandleService(
            PlatformHandleRepository handleRepository,
            UserRepository userRepository,
            DailySnapshotRepository dailySnapshotRepository) {

        this.handleRepository = handleRepository;
        this.userRepository = userRepository;
        this.dailySnapshotRepository = dailySnapshotRepository;
    }

    public PlatformHandle addHandle(UUID userId, String platform, String handle) {

        User user = userRepository.findByAuthUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if this platform already exists for this user
        PlatformHandle ph = handleRepository
                .findByUserAndPlatform(user, platform)
                .orElse(new PlatformHandle());

        ph.setUser(user);
        ph.setPlatform(platform);
        ph.setHandle(handle);

        return handleRepository.save(ph);
    }

    public List<PlatformHandle> getHandlesForUser(UUID authUserId) {

        User user = userRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return handleRepository.findByUser(user);
    }

    @Transactional
    public void deleteHandle(UUID handleId, UUID authUserId) {

        User user = userRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        PlatformHandle handle = handleRepository.findById(handleId)
                .orElseThrow(() -> new RuntimeException("Handle not found"));

        if (!handle.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        // Delete snapshots first
        dailySnapshotRepository.deleteByHandleId(handleId);

        // Then delete handle
        handleRepository.delete(handle);
    }

    @Transactional
    public PlatformHandle updateHandle(UUID handleId, UUID userId, String platform, String handle) {
        PlatformHandle existing = handleRepository.findById(handleId)
                .orElseThrow(() -> new RuntimeException("Handle not found"));
        // Verify ownership
        if (!existing.getUser().getAuthUserId().equals(userId)) {
            throw new RuntimeException("You do not own this handle");
        }
        existing.setHandle(handle);
        // Optionally update platform if needed, but we assume it stays the same
        return handleRepository.save(existing);
    }
}