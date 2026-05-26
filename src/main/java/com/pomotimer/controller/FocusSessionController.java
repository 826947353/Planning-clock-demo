package com.pomotimer.controller;

import com.pomotimer.dto.FocusSessionDTO;
import com.pomotimer.service.FocusSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/focus")
@RequiredArgsConstructor
public class FocusSessionController {

    private final FocusSessionService focusSessionService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FocusSessionDTO>> getUserSessions(@PathVariable Long userId) {
        return ResponseEntity.ok(focusSessionService.getUserSessions(userId));
    }

    @GetMapping("/user/{userId}/range")
    public ResponseEntity<List<FocusSessionDTO>> getSessionsByRange(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(focusSessionService.getSessionsByDateRange(userId, start, end));
    }

    @PostMapping("/start")
    public ResponseEntity<FocusSessionDTO> startSession(@RequestBody FocusSessionDTO dto) {
        return ResponseEntity.ok(focusSessionService.startSession(dto));
    }

    @PutMapping("/{sessionId}/complete")
    public ResponseEntity<FocusSessionDTO> completeSession(
            @PathVariable Long sessionId,
            @RequestParam(required = false) Integer actualMinutes,
            @RequestParam(required = false) String moodAfter) {
        return ResponseEntity.ok(focusSessionService.completeSession(sessionId, actualMinutes, moodAfter));
    }

    @PutMapping("/{sessionId}/abandon")
    public ResponseEntity<FocusSessionDTO> abandonSession(@PathVariable Long sessionId) {
        return ResponseEntity.ok(focusSessionService.abandonSession(sessionId));
    }
}