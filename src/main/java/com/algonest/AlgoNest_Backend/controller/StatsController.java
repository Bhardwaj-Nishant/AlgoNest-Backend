package com.algonest.AlgoNest_Backend.controller;

import com.algonest.AlgoNest_Backend.dto.DailySnapshotDTO;
import com.algonest.AlgoNest_Backend.dto.HeatmapDayDTO;
import com.algonest.AlgoNest_Backend.entity.DailySnapshot;
import com.algonest.AlgoNest_Backend.entity.HeatmapDay;
import com.algonest.AlgoNest_Backend.service.StatsService;
import com.algonest.AlgoNest_Backend.util.AuthUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService statsService;
    private final AuthUtil authUtil;

    public StatsController(StatsService statsService, AuthUtil authUtil) {
        this.statsService = statsService;
        this.authUtil = authUtil;
    }

    @GetMapping("/latest")
    public ResponseEntity<List<DailySnapshotDTO>> getLatestStats(@RequestParam(required = false) String platform) {
        UUID userId = authUtil.getCurrentUserId();
        List<DailySnapshot> snapshots;
        if (platform != null && !platform.isEmpty()) {
            snapshots = statsService.getLatestStats(userId, platform);

            System.out.println("Snapshots found = " + snapshots.size());

            for (DailySnapshot ds : snapshots) {
                System.out.println("ID = " + ds.getId());
                System.out.println("Solved = " + ds.getTotalSolved());
                System.out.println("Rating = " + ds.getRating());
                System.out.println("Active Days = " + ds.getActiveDays());
                System.out.println("Platform = " + ds.getPlatformHandle().getPlatform());
            }
        } else {
            snapshots = statsService.getLatestStats(userId, null);
        }
        return ResponseEntity.ok(snapshots.stream().map(this::convertToDTO).collect(Collectors.toList()));
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