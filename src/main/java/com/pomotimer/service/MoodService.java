package com.pomotimer.service;

import com.pomotimer.dto.MoodDTO;
import com.pomotimer.entity.MoodRecord;
import com.pomotimer.repository.MoodRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MoodService {

    private final MoodRecordRepository moodRecordRepository;

    public List<MoodDTO> getUserMoods(Long userId) {
        return moodRecordRepository.findByUserIdOrderByRecordedAtDesc(userId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<MoodDTO> getMoodsByDateRange(Long userId, LocalDateTime start, LocalDateTime end) {
        return moodRecordRepository.findByUserIdAndDateRange(userId, start, end)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public MoodDTO getLatestMood(Long userId) {
        MoodRecord record = moodRecordRepository.findTopByUserIdOrderByRecordedAtDesc(userId);
        return record != null ? toDTO(record) : null;
    }

    public MoodDTO recordMood(MoodDTO dto) {
        MoodRecord record = MoodRecord.builder()
                .userId(dto.getUserId())
                .mood(dto.getMood())
                .intensity(dto.getIntensity())
                .note(dto.getNote())
                .recordedAt(LocalDateTime.now())
                .build();
        return toDTO(moodRecordRepository.save(record));
    }

    private MoodDTO toDTO(MoodRecord entity) {
        return MoodDTO.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .mood(entity.getMood())
                .intensity(entity.getIntensity())
                .note(entity.getNote())
                .recordedAt(entity.getRecordedAt())
                .build();
    }
}