package com.pomotimer.controller;

import com.pomotimer.dto.GrowthDTO;
import com.pomotimer.service.GrowthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/growth")
@RequiredArgsConstructor
public class GrowthController {

    private final GrowthService growthService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<GrowthDTO>> getUserGrowth(@PathVariable Long userId) {
        return ResponseEntity.ok(growthService.getUserGrowth(userId));
    }
}