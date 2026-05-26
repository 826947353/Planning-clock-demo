package com.pomotimer.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationDTO {
    private Long id;
    private String title;
    private Integer durationMinutes;
    private String category;
    private String description;
    private String icon;
    private List<String> suggestedResources;
}