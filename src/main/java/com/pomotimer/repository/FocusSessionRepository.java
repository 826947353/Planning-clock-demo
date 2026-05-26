package com.pomotimer.repository;

import com.pomotimer.entity.FocusSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FocusSessionRepository extends JpaRepository<FocusSession, Long> {

    List<FocusSession> findByUserIdOrderByStartTimeDesc(Long userId);

    List<FocusSession> findByUserIdAndStatus(Long userId, FocusSession.SessionStatus status);

    @Query("SELECT f FROM FocusSession f WHERE f.userId = :userId AND f.startTime BETWEEN :start AND :end ORDER BY f.startTime DESC")
    List<FocusSession> findByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(f.actualMinutes), 0) FROM FocusSession f WHERE f.userId = :userId AND f.status = 'COMPLETED'")
    Integer getTotalFocusMinutes(@Param("userId") Long userId);

    long countByUserIdAndStatus(Long userId, FocusSession.SessionStatus status);
}