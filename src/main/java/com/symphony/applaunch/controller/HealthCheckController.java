package com.symphony.applaunch.controller;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.symphony.applaunch.service.IHealthCheckService;

@RestController
@RequestMapping("/healthcheck")
@RequiredArgsConstructor
public class HealthCheckController {

	private static final Logger log = LoggerFactory.getLogger(HealthCheckController.class);

	private final IHealthCheckService healthService;

	@GetMapping("/status")
	public String getHealthStatus() {
		log.info("HealthCheckController :: getHealthStatus() called!");
		boolean isHealthy = healthService.checkApplicationHealth();

		if (isHealthy) {
			return "APP LAUNCH-SERVICE - HEALTHY";
		} else {
			return "APP LAUNCH-SERVICE - UNHEALTHY";
		}
	}
}
