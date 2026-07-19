package com.algonest.AlgoNest_Backend.service;

import com.algonest.AlgoNest_Backend.client.ScraperClient;
import com.algonest.AlgoNest_Backend.dto.ScrapeResponse;
import com.algonest.AlgoNest_Backend.entity.DailySnapshot;
import com.algonest.AlgoNest_Backend.entity.HeatmapDay;
import com.algonest.AlgoNest_Backend.entity.PlatformHandle;
import com.algonest.AlgoNest_Backend.repository.DailySnapshotRepository;
import com.algonest.AlgoNest_Backend.repository.PlatformHandleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Slf4j
public class SyncService {

    private final ScraperClient scraperClient;
    private final PlatformHandleRepository handleRepository;
    private final DailySnapshotRepository snapshotRepository;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    public SyncService(ScraperClient scraperClient,
                       PlatformHandleRepository handleRepository,
                       DailySnapshotRepository snapshotRepository) {
        this.scraperClient = scraperClient;
        this.handleRepository = handleRepository;
        this.snapshotRepository = snapshotRepository;
    }

    @Scheduled(fixedDelay = 900000) // 15 minutes
    public void scheduledSyncAll() {
        if (!isRunning.compareAndSet(false, true)) {
            log.warn("Previous sync still running, skipping this run.");
            return;
        }
        try {
            List<PlatformHandle> handles = handleRepository.findAll();
            log.info("Starting scheduled sync for {} handles.", handles.size());
            for (PlatformHandle handle : handles) {
                syncHandle(handle);
            }
            log.info("Scheduled sync completed.");
        } catch (Exception e) {
            log.error("Scheduled sync failed: {}", e.getMessage(), e);
        } finally {
            isRunning.set(false);
        }
    }

    @Transactional
    public void syncHandle(PlatformHandle handle) {
        try {
            ScrapeResponse response = scraperClient.scrapePlatform(handle.getPlatform(), handle.getHandle())
                    .block();

            log.info("Response object = {}", response);
            if (response != null) {
                log.info("Solved = {}", response.getTotalSolved());
                log.info("Rating = {}", response.getRating());
                log.info("Difficulty = {}", response.getDifficultyCounts());
            }

            if (response == null) {
                log.error("No response for {}:{}", handle.getPlatform(), handle.getHandle());
                saveOrUpdateSnapshot(handle, null, "No response from scraper");
                return;
            }

            if (response.getError() != null && !response.getError().isEmpty()) {
                log.error("Scraper error for {}: {}", handle.getHandle(), response.getError());
                saveOrUpdateSnapshot(handle, response, response.getError());
                return;
            }

            // If no data, save error
            if (response.getTotalSolved() == null && response.getHeatmapData() == null) {
                log.warn("Scraper returned empty data for {}", handle.getHandle());
                saveOrUpdateSnapshot(handle, response, "No data found for this user");
                return;
            }

            // Save or update snapshot
            saveOrUpdateSnapshot(handle, response, null);
            log.info("Sync successful for {}:{}", handle.getPlatform(), handle.getHandle());

        } catch (Exception e) {
            log.error("Exception during sync for {}: {}", handle.getHandle(), e.getMessage());
            saveOrUpdateSnapshot(handle, null, "Sync exception: " + e.getMessage());
        }
    }

    // ✅ Upsert logic with createdAt update
    private void saveOrUpdateSnapshot(PlatformHandle handle, ScrapeResponse response, String errorMessage) {
        LocalDate today = LocalDate.now();
        Optional<DailySnapshot> existing = snapshotRepository.findByPlatformHandleIdAndSnapshotDate(
                handle.getId(), today);

        DailySnapshot snapshot;
        if (existing.isPresent()) {
            snapshot = existing.get();
            log.info("Updating existing snapshot for {} on {}", handle.getHandle(), today);
        } else {
            snapshot = new DailySnapshot();
            snapshot.setPlatformHandle(handle);
            snapshot.setSnapshotDate(today);
            log.info("Creating new snapshot for {} on {}", handle.getHandle(), today);
        }

        // Update fields from response
        if (response != null) {
            snapshot.setTotalSolved(response.getTotalSolved());
            snapshot.setRating(response.getRating());
            snapshot.setContestGiven(response.getContestGiven());
            snapshot.setMaxStreakLifetime(response.getMaxStreakLifetime());
            snapshot.setMaxStreakCurrentYear(response.getMaxStreakCurrentYear());
            snapshot.setActiveDays(response.getActiveDays());
            snapshot.setAcceptanceRate(response.getAcceptanceRate());
            snapshot.setTotalRepos(response.getTotalRepos());
            snapshot.setTotalContributions(response.getTotalContributions());
            snapshot.setDifficultyCounts(response.getDifficultyCounts());
            snapshot.setSolvedQuestionsByCategory(response.getSolvedQuestionsByCategory());

            // Convert HeatmapDayDTO list to entity list
            if (response.getHeatmapData() != null) {
                List<HeatmapDay> heatmapDays = response.getHeatmapData().stream()
                        .map(dto -> new HeatmapDay(dto.getDate(), dto.getCount()))
                        .toList();
                snapshot.setHeatmapData(heatmapDays);
            } else {
                snapshot.setHeatmapData(null);
            }
        }

        snapshot.setErrorMessage(errorMessage);

        // ✅ Update the createdAt timestamp to NOW so we know when this snapshot was last synced
        snapshot.setCreatedAt(LocalDateTime.now());

        snapshotRepository.save(snapshot);
    }

    // Manual sync all
    public void syncAllNow() {
        scheduledSyncAll();
    }
}