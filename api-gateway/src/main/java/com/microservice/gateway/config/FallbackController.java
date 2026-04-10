package com.microservice.gateway.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

	@GetMapping("/auth")
	public ResponseEntity<Map<String, Object>> authFallback() {
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
				.body(Map.of("error", "SERVICE_UNAVAILABLE", "message",
						"Auth service is temporarily unavailable. Please try again.", "timestamp",
						Instant.now().toString()));
	}

	@GetMapping("/main")
	public ResponseEntity<Map<String, Object>> mainFallback() {
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
				.body(Map.of("error", "SERVICE_UNAVAILABLE", "message",
						"Main service is temporarily unavailable. Please try again.", "timestamp",
						Instant.now().toString()));
	}
}
