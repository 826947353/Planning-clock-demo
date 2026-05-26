package com.pomotimer.service;

import com.pomotimer.dto.GrowthDTO;
import com.pomotimer.entity.GrowthRecord;
import com.pomotimer.repository.GrowthRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GrowthService {

    private final GrowthRecordRepository growthRecordRepository;

    public List<GrowthDTO> getUserGrowth(Long userId) {
        return growthRecordRepository.findByUserIdOrderByEventTimeDesc(userId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    private GrowthDTO toDTO(GrowthRecord entity) {
        return GrowthDTO.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .eventType(entity.getEventType())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .leavesEarned(entity.getLeavesEarned())
                .eventTime(entity.getEventTime())
                .build();
    }
}