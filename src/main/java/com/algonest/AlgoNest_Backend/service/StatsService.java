package com.algonest.AlgoNest_Backend.service;

import com.algonest.AlgoNest_Backend.entity.DailySnapshot;
import com.algonest.AlgoNest_Backend.repository.DailySnapshotRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class StatsService {

    private final DailySnapshotRepository snapshotRepository;

    public StatsService(DailySnapshotRepository snapshotRepository) {
        this.snapshotRepository = snapshotRepository;
    }

    public List<DailySnapshot> getLatestStats(UUID userId, String platform) {
        if (platform != null && !platform.isEmpty()) {
            return snapshotRepository.findLatestByUserIdAndPlatform(userId, platform);
        }
        return snapshotRepository.findLatestByUserId(userId);
    }

    public List<DailySnapshot> getHistoryForHandle(UUID handleId) {
        return snapshotRepository.findByPlatformHandleIdOrderBySnapshotDateAsc(handleId);
    }
}