package com.microservice.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservice.gateway.exception.ErrorCode;
import com.microservice.gateway.exception.ServiceResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@Slf4j
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

	private final WebClient.Builder webClientBuilder;
	private final ObjectMapper objectMapper = new ObjectMapper();

	public JwtAuthenticationFilter(WebClient.Builder webClientBuilder) {
		super(Config.class);
		this.webClientBuilder = webClientBuilder;
	}

	@Override
	public GatewayFilter apply(Config config) {
		return (exchange, chain) -> {
			String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

			if (authHeader == null || !authHeader.startsWith("Bearer ")) {
				return unauthorizedResponse(exchange, "Missing or invalid Authorization header. Use Bearer <token>");
			}

			// Call Auth Service for token validation (using service discovery)
			return webClientBuilder.build().post().uri("lb://auth-service/public/token/validate") // Good use of lb://
					.header(HttpHeaders.AUTHORIZATION, authHeader).retrieve().bodyToMono(Map.class)
					.flatMap(response -> {
						// Extract claims from auth service response
						String userId = (String) response.get("userId");
						String tenantId = (String) response.get("tenantId");
						String username = (String) response.get("username");

						if (userId == null || tenantId == null) {
							return unauthorizedResponse(exchange, "Invalid token claims from auth service");
						}

						// Add headers for downstream services (main/facility service)
						var mutatedRequest = exchange.getRequest().mutate().header("X-User-Id", userId)
								.header("X-Tenant-Id", tenantId).header("X-Username", username != null ? username : "")
								.header("X-User-Role", "USER").build();

						log.info("Token validated successfully for userId={}, tenantId={}", userId, tenantId);

						return chain.filter(exchange.mutate().request(mutatedRequest).build());
					}).onErrorResume(WebClientResponseException.class, e -> {
						log.warn("Token validation failed. Status: {}, Body: {}", e.getStatusCode(),
								e.getResponseBodyAsString());
						return unauthorizedResponse(exchange, "Invalid or expired token");
					}).onErrorResume(Exception.class, e -> {
						log.error("Auth service unreachable or error during validation", e);
						return unauthorizedResponse(exchange, "Authentication service unavailable. Please try later.");
					});
		};
	}

	/**
	 * Returns consistent JSON error matching your ServiceResponse format
	 */
	private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(HttpStatus.UNAUTHORIZED);
		response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

		ServiceResponse errorResponse = ServiceResponse.asFailure(ErrorCode.UNAUTHORIZED, message);

		try {
			byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
			return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
		} catch (Exception ex) {
			log.error("Failed to serialize error response", ex);
			// Fallback simple response
			String fallback = "{\"error\":true,\"errorCode\":\"UNAUTHORIZED\",\"errorMessage\":\"Authentication failed\",\"data\":null}";
			return response.writeWith(Mono.just(response.bufferFactory().wrap(fallback.getBytes())));
		}
	}

	public static class Config {
		// You can add configuration properties here later if needed
	}
}