package com.pomotimer.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrowthDTO {
    private Long id;
    private Long userId;
    private String eventType;
    private String title;
    private String description;
    private Integer leavesEarned;
    private LocalDateTime eventTime;
}