package com.aro.aro_gestao.controller;

import com.aro.aro_gestao.dto.DashboardResponseDTO;
import com.aro.aro_gestao.service.DashboardService;
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

    @GetMapping
    public ResponseEntity<DashboardResponseDTO> buscarResumo() {
        return ResponseEntity.ok(dashboardService.buscarResumo());
    }
}
