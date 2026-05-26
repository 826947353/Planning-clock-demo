package com.pomotimer.controller;

import com.pomotimer.dto.RecommendationDTO;
import com.pomotimer.service.AiSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendation")
@RequiredArgsConstructor
public class RecommendationController {

    private final AiSummaryService aiSummaryService;

    @GetMapping
    public ResponseEntity<List<RecommendationDTO>> getRecommendations(
            @RequestParam(defaultValue = "微疲惫") String mood) {
        return ResponseEntity.ok(aiSummaryService.getRecommendations(mood));
    }
}