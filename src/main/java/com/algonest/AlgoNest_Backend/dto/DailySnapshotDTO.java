package com.algonest.AlgoNest_Backend.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class DailySnapshotDTO {
    private String id;
    private LocalDate snapshotDate;
    private Integer totalSolved;
    private Integer rating;
    private Integer contestGiven;
    private Integer maxStreakLifetime;
    private Integer maxStreakCurrentYear;
    private Integer activeDays;
    private Double acceptanceRate;
    private Integer totalRepos;
    private Integer totalContributions;
    private Map<String, Integer> difficultyCounts;
    private Map<String, List<String>> solvedQuestionsByCategory;
    private List<HeatmapDayDTO> heatmapData;
    private String errorMessage;
    private String platform;
    private String handle;
    private LocalDateTime syncedAt;
}