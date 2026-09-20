package com.kimngeam.backend.admin.dashboard;

import com.kimngeam.backend.admin.dashboard.dto.DashboardResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Requiere rol {@code admin} (ver {@code SecurityConfig}, matcher
 * {@code /admin/**} desde Fase 1).
 */
@RestController
@RequestMapping("/admin/dashboard")
public class DashboardController {

	private final DashboardService dashboardService;

	public DashboardController(DashboardService dashboardService) {
		this.dashboardService = dashboardService;
	}

	@GetMapping
	public ResponseEntity<DashboardResponse> obtener() {
		return ResponseEntity.ok(dashboardService.obtener());
	}
}
