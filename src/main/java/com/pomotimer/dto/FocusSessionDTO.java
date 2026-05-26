package com.pomotimer.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FocusSessionDTO {
    private Long id;
    private Long userId;
    private String title;
    private String description;
    private Integer plannedMinutes;
    private Integer actualMinutes;
    private String sessionType;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String aiSummary;
    private String moodBefore;
    private String moodAfter;
    private LocalDateTime createdAt;
}