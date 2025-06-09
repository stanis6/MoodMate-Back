// src/main/java/com/example/app/controller/DashboardController.java
package com.example.app.controller;

import com.example.app.dto.DashboardDto;
import com.example.app.service.DashboardService;
import com.example.app.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/teacher")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDto> getDashboard(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        UUID teacherId = JwtUtil.extractUserId(token);

        DashboardDto dto = dashboardService.getDashboardForTeacher(teacherId);
        return ResponseEntity.ok(dto);
    }
}
