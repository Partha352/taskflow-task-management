package com.taskflow.backend.controller;

import com.taskflow.backend.dto.AdminDashboardResponse;
import com.taskflow.backend.dto.UserDashboardResponse;
import com.taskflow.backend.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/user")
    public ResponseEntity<UserDashboardResponse> userDashboard() {
        return ResponseEntity.ok(dashboardService.userDashboard());
    }

    @GetMapping("/admin")
    public ResponseEntity<AdminDashboardResponse> adminDashboard() {
        return ResponseEntity.ok(dashboardService.adminDashboard());
    }
}
