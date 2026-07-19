package com.algonest.AlgoNest_Backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Data
@Table(
        name = "daily_snapshot",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"handle_id", "snapshot_date"})
        }
)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class DailySnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "handle_id", nullable = false)
    private PlatformHandle platformHandle;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    // ----- Core Stats (Primitives) -----
    private Integer totalSolved;
    private Integer rating;
    private Integer contestGiven;
    private Integer maxStreakLifetime;
    private Integer maxStreakCurrentYear;
    private Integer activeDays;
    private Double acceptanceRate;      // Only for LeetCode
    private Integer totalRepos;         // Only for GitHub
    private Integer totalContributions; // Only for GitHub

    // ----- JSONB Fields (Complex Structures) -----
    // Map: {"easy": 10, "medium": 5, "hard": 2}
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Integer> difficultyCounts;

    // Map: {"easy": ["Two Sum", "Valid Parentheses"], "medium": [...]}
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, List<String>> solvedQuestionsByCategory;

    // List: [{"date": "2026-07-14", "count": 3}, ...]
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<HeatmapDay> heatmapData;

    // Optional: Catch-all for extra fields (e.g., LeetCode's skill breakdown)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> rawData;

    // If the Python scraper failed for this specific run
    private String errorMessage;

    // Optional: Timestamp for when this snapshot was created
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

}