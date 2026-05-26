package com.pomotimer.controller;

import com.pomotimer.dto.FocusSessionDTO;
import com.pomotimer.service.AiSummaryService;
import com.pomotimer.service.FocusSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiSummaryController {

    private final AiSummaryService aiSummaryService;
    private final FocusSessionService focusSessionService;

    @PostMapping("/summary/{sessionId}")
    public ResponseEntity<FocusSessionDTO> generateSummary(@PathVariable Long sessionId) {
        String summary = aiSummaryService.generateAiSummary(sessionId);
        return ResponseEntity.ok(focusSessionService.updateAiSummary(sessionId, summary));
    }
}