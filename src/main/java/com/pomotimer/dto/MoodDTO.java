package com.pomotimer.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoodDTO {
    private Long id;
    private Long userId;
    private String mood;
    private Integer intensity;
    private String note;
    private LocalDateTime recordedAt;
}