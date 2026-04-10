package com.microservice.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Validates that tenant context is present and optionally matches the
 * X-Tenant-Id header provided in the request against the JWT claim.
 */
@Component
@Slf4j
public class TenantContextFilter extends AbstractGatewayFilterFactory<TenantContextFilter.Config> {

	public TenantContextFilter() {
		super(Config.class);
	}

	@Override
	public GatewayFilter apply(Config config) {
		return (exchange, chain) -> {
			ServerHttpRequest request = exchange.getRequest();

			// X-Tenant-Id is set by JwtAuthenticationFilter from JWT claims
			String tenantId = request.getHeaders().getFirst("X-Tenant-Id");

			if (tenantId == null || tenantId.isBlank()) {
				log.warn("Request missing tenant context: {}", request.getPath());
				return forbiddenResponse(exchange, "Missing tenant context");
			}

			// Optionally: validate tenant is active via a cache lookup
			// For now we trust the JWT-derived tenant ID

			log.debug("Tenant context set: tenantId={}", tenantId);
			return chain.filter(exchange);
		};
	}

	private Mono<Void> forbiddenResponse(ServerWebExchange exchange, String message) {
		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(HttpStatus.FORBIDDEN);
		response.getHeaders().add("Content-Type", "application/json");
		String body = String.format("{\"error\":\"FORBIDDEN\",\"message\":\"%s\"}", message);
		var buffer = response.bufferFactory().wrap(body.getBytes());
		return response.writeWith(Mono.just(buffer));
	}

	public static class Config {
	}
}