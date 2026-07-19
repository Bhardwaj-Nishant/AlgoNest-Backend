package com.algonest.AlgoNest_Backend.controller;

import com.algonest.AlgoNest_Backend.dto.DailySnapshotDTO;
import com.algonest.AlgoNest_Backend.dto.HeatmapDayDTO;
import com.algonest.AlgoNest_Backend.entity.DailySnapshot;
import com.algonest.AlgoNest_Backend.entity.HeatmapDay;
import com.algonest.AlgoNest_Backend.entity.PlatformHandle;
import com.algonest.AlgoNest_Backend.repository.DailySnapshotRepository;
import com.algonest.AlgoNest_Backend.repository.PlatformHandleRepository;
import com.algonest.AlgoNest_Backend.service.SyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sync")
public class SyncController {

    private final SyncService syncService;
    private final PlatformHandleRepository handleRepository;
    private final DailySnapshotRepository snapshotRepository;

    public SyncController(SyncService syncService,
                          PlatformHandleRepository handleRepository,
                          DailySnapshotRepository snapshotRepository) {
        this.syncService = syncService;
        this.handleRepository = handleRepository;
        this.snapshotRepository = snapshotRepository;
    }

    @PostMapping("/handle/{id}")
    public ResponseEntity<?> syncHandle(@PathVariable UUID id) {
        PlatformHandle handle = handleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Handle not found"));

        // 1. Trigger sync (blocking)
        syncService.syncHandle(handle);

        // 2. Fetch snapshot using handle.id and today's date – most reliable
        LocalDate today = LocalDate.now();
        Optional<DailySnapshot> snapshotOpt = snapshotRepository.findByPlatformHandleIdAndSnapshotDate(handle.getId(), today);

        if (snapshotOpt.isPresent()) {
            return ResponseEntity.ok(convertToDTO(snapshotOpt.get()));
        }

        // 3. Fallback: try latest by platform (in case date is off)
        List<DailySnapshot> snapshots = snapshotRepository.findLatestByUserIdAndPlatform(
                handle.getUser().getId(),
                handle.getPlatform()
        );
        if (!snapshots.isEmpty()) {
            return ResponseEntity.ok(convertToDTO(snapshots.get(0)));
        }

        // 4. No data yet
        return ResponseEntity.ok(Map.of("message", "Sync triggered, but no data yet"));
    }

    @PostMapping("/all")
    public ResponseEntity<String> syncAll() {
        syncService.syncAllNow();
        return ResponseEntity.ok("Sync triggered for all handles.");
    }

    // ----- Conversion Methods -----
    private DailySnapshotDTO convertToDTO(DailySnapshot snapshot) {
        if (snapshot == null) return null;
        DailySnapshotDTO dto = new DailySnapshotDTO();
        dto.setId(snapshot.getId().toString());
        dto.setSnapshotDate(snapshot.getSnapshotDate());
        dto.setTotalSolved(snapshot.getTotalSolved());
        dto.setRating(snapshot.getRating());
        dto.setContestGiven(snapshot.getContestGiven());
        dto.setMaxStreakLifetime(snapshot.getMaxStreakLifetime());
        dto.setMaxStreakCurrentYear(snapshot.getMaxStreakCurrentYear());
        dto.setActiveDays(snapshot.getActiveDays());
        dto.setAcceptanceRate(snapshot.getAcceptanceRate());
        dto.setTotalRepos(snapshot.getTotalRepos());
        dto.setTotalContributions(snapshot.getTotalContributions());
        dto.setDifficultyCounts(snapshot.getDifficultyCounts());
        dto.setSolvedQuestionsByCategory(snapshot.getSolvedQuestionsByCategory());
        dto.setHeatmapData(convertHeatmapList(snapshot.getHeatmapData()));
        dto.setErrorMessage(snapshot.getErrorMessage());
        dto.setSyncedAt(snapshot.getCreatedAt());
        if (snapshot.getPlatformHandle() != null) {
            dto.setPlatform(snapshot.getPlatformHandle().getPlatform());
            dto.setHandle(snapshot.getPlatformHandle().getHandle());
        }
        return dto;
    }

    private List<HeatmapDayDTO> convertHeatmapList(List<HeatmapDay> heatmapDays) {
        if (heatmapDays == null) return null;
        return heatmapDays.stream()
                .map(day -> new HeatmapDayDTO(day.getDate(), day.getCount()))
                .collect(Collectors.toList());
    }
}