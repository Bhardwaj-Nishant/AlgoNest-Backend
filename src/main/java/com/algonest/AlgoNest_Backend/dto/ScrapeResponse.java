package com.algonest.AlgoNest_Backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ScrapeResponse {
    private String platform;
    private String handle;

    // Core stats
    private Integer totalSolved;
    private Integer rating;
    private Integer contestGiven;
    private Double acceptanceRate;

    // Difficulty breakdown
    private Map<String, Integer> difficultyCounts;

    // Solved questions by category
    private Map<String, List<String>> solvedQuestionsByCategory;

    // Heatmap
    private List<HeatmapDayDTO> heatmapData;

    // Activity
    private Integer maxStreakLifetime;
    private Integer maxStreakCurrentYear;
    private Integer activeDays;
    private CalendarInfoDTO calendarInfo;

    // GitHub specific
    private Integer totalRepos;
    private Integer totalContributions;

    // LeetCode specific
    private Map<String, Object> skillBreakdown;
    private Object contestRanking;
    private List<Object> contestHistory;

    // Error
    private String error;
}