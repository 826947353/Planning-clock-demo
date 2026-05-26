package com.pomotimer.repository;

import com.pomotimer.entity.GrowthRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GrowthRecordRepository extends JpaRepository<GrowthRecord, Long> {

    List<GrowthRecord> findByUserIdOrderByEventTimeDesc(Long userId);

    List<GrowthRecord> findByUserIdAndEventType(Long userId, String eventType);
}