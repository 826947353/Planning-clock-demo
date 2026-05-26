package com.pomotimer.repository;

import com.pomotimer.entity.MoodRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MoodRecordRepository extends JpaRepository<MoodRecord, Long> {

    List<MoodRecord> findByUserIdOrderByRecordedAtDesc(Long userId);

    @Query("SELECT m FROM MoodRecord m WHERE m.userId = :userId AND m.recordedAt BETWEEN :start AND :end ORDER BY m.recordedAt ASC")
    List<MoodRecord> findByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    MoodRecord findTopByUserIdOrderByRecordedAtDesc(Long userId);
}