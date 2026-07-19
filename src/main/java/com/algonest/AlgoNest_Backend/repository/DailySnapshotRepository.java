package com.algonest.AlgoNest_Backend.repository;

import com.algonest.AlgoNest_Backend.entity.DailySnapshot;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DailySnapshotRepository extends JpaRepository<DailySnapshot, UUID> {

    @Query("""
        SELECT ds
        FROM DailySnapshot ds
        WHERE ds.platformHandle.user.authUserId = :userId
        AND ds.snapshotDate = (
            SELECT MAX(ds2.snapshotDate)
            FROM DailySnapshot ds2
            WHERE ds2.platformHandle = ds.platformHandle
        )
    """)
    List<DailySnapshot> findLatestByUserId(@Param("userId") UUID userId);

    @Query("SELECT ds FROM DailySnapshot ds " +
            "WHERE ds.platformHandle.user.authUserId = :userId " +
            "AND ds.platformHandle.platform = :platform " +
            "AND ds.snapshotDate = (" +
            "    SELECT MAX(ds2.snapshotDate) " +
            "    FROM DailySnapshot ds2 " +
            "    WHERE ds2.platformHandle = ds.platformHandle " +
            "    AND ds2.platformHandle.platform = :platform" +
            ")")

    List<DailySnapshot> findLatestByUserIdAndPlatform(@Param("userId") UUID userId,
                                                      @Param("platform") String platform);

    @Transactional
    @Modifying
    @Query("""
        DELETE FROM DailySnapshot d
        WHERE d.platformHandle.id = :handleId
    """)
    void deleteByHandleId(UUID handleId);


    List<DailySnapshot> findByPlatformHandleIdOrderBySnapshotDateAsc(UUID handleId);

    // ✅ ADD THIS METHOD
    Optional<DailySnapshot> findByPlatformHandleIdAndSnapshotDate(UUID handleId, LocalDate snapshotDate);


}