package com.pomotimer.controller;

import com.pomotimer.dto.MoodDTO;
import com.pomotimer.service.MoodService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/mood")
@RequiredArgsConstructor
public class MoodController {

    private final MoodService moodService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<MoodDTO>> getUserMoods(@PathVariable Long userId) {
        return ResponseEntity.ok(moodService.getUserMoods(userId));
    }

    @GetMapping("/user/{userId}/latest")
    public ResponseEntity<MoodDTO> getLatestMood(@PathVariable Long userId) {
        return ResponseEntity.ok(moodService.getLatestMood(userId));
    }

    @GetMapping("/user/{userId}/range")
    public ResponseEntity<List<MoodDTO>> getMoodsByRange(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(moodService.getMoodsByDateRange(userId, start, end));
    }

    @PostMapping
    public ResponseEntity<MoodDTO> recordMood(@RequestBody MoodDTO dto) {
        return ResponseEntity.ok(moodService.recordMood(dto));
    }
}