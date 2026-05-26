package com.pomotimer.service;

import com.pomotimer.dto.FocusSessionDTO;
import com.pomotimer.dto.GrowthDTO;
import com.pomotimer.entity.FocusSession;
import com.pomotimer.entity.GrowthRecord;
import com.pomotimer.entity.User;
import com.pomotimer.repository.FocusSessionRepository;
import com.pomotimer.repository.GrowthRecordRepository;
import com.pomotimer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FocusSessionService {

    private final FocusSessionRepository focusSessionRepository;
    private final UserRepository userRepository;
    private final GrowthRecordRepository growthRecordRepository;

    public List<FocusSessionDTO> getUserSessions(Long userId) {
        return focusSessionRepository.findByUserIdOrderByStartTimeDesc(userId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<FocusSessionDTO> getSessionsByDateRange(Long userId, LocalDateTime start, LocalDateTime end) {
        return focusSessionRepository.findByUserIdAndDateRange(userId, start, end)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public FocusSessionDTO startSession(FocusSessionDTO dto) {
        FocusSession session = FocusSession.builder()
                .userId(dto.getUserId())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .plannedMinutes(dto.getPlannedMinutes())
                .sessionType(FocusSession.SessionType.valueOf(dto.getSessionType()))
                .status(FocusSession.SessionStatus.RUNNING)
                .startTime(LocalDateTime.now())
                .moodBefore(dto.getMoodBefore())
                .build();
        FocusSession saved = focusSessionRepository.save(session);
        return toDTO(saved);
    }

    @Transactional
    public FocusSessionDTO completeSession(Long sessionId, Integer actualMinutes, String moodAfter) {
        FocusSession session = focusSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

        session.setStatus(FocusSession.SessionStatus.COMPLETED);
        session.setEndTime(LocalDateTime.now());
        session.setActualMinutes(actualMinutes != null ? actualMinutes : session.getPlannedMinutes());
        session.setMoodAfter(moodAfter);

        User user = userRepository.findById(session.getUserId()).orElse(null);
        if (user != null) {
            user.setTotalFocusMinutes(user.getTotalFocusMinutes() + session.getActualMinutes());

            int leaves = session.getActualMinutes() / 25;
            user.setTotalLeaves(user.getTotalLeaves() + leaves);
            userRepository.save(user);

            GrowthRecord growth = GrowthRecord.builder()
                    .userId(user.getId())
                    .eventType("FOCUS_COMPLETED")
                    .title("完成专注: " + session.getTitle())
                    .description("专注了 " + session.getActualMinutes() + " 分钟")
                    .leavesEarned(leaves)
                    .eventTime(LocalDateTime.now())
                    .build();
            growthRecordRepository.save(growth);
        }

        FocusSession saved = focusSessionRepository.save(session);
        return toDTO(saved);
    }

    @Transactional
    public FocusSessionDTO abandonSession(Long sessionId) {
        FocusSession session = focusSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));
        session.setStatus(FocusSession.SessionStatus.ABANDONED);
        session.setEndTime(LocalDateTime.now());
        return toDTO(focusSessionRepository.save(session));
    }

    @Transactional
    public FocusSessionDTO updateAiSummary(Long sessionId, String summary) {
        FocusSession session = focusSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));
        session.setAiSummary(summary);
        return toDTO(focusSessionRepository.save(session));
    }

    private FocusSessionDTO toDTO(FocusSession entity) {
        return FocusSessionDTO.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .plannedMinutes(entity.getPlannedMinutes())
                .actualMinutes(entity.getActualMinutes())
                .sessionType(entity.getSessionType().name())
                .status(entity.getStatus().name())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .aiSummary(entity.getAiSummary())
                .moodBefore(entity.getMoodBefore())
                .moodAfter(entity.getMoodAfter())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}