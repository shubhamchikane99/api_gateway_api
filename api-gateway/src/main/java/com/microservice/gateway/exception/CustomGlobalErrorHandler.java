package com.microservice.gateway.exception;

import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

@Component
@Order(-2)
public class CustomGlobalErrorHandler extends AbstractErrorWebExceptionHandler {

    public CustomGlobalErrorHandler(
            ErrorAttributes errorAttributes,
            WebProperties webProperties,                    // Changed: Use WebProperties instead of Resources directly
            ApplicationContext applicationContext,
            ServerCodecConfigurer serverCodecConfigurer) {

        super(errorAttributes, webProperties.getResources(), applicationContext);
        setMessageWriters(serverCodecConfigurer.getWriters());
        setMessageReaders(serverCodecConfigurer.getReaders());
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::formatErrorResponse);
    }

    private Mono<ServerResponse> formatErrorResponse(ServerRequest request) {

        Throwable error = getError(request);
        HttpStatus status = HttpStatus.NOT_FOUND;

        if (error instanceof org.springframework.web.server.ResponseStatusException rse) {
            status = (HttpStatus) rse.getStatusCode();
        }

        // Your desired clean response
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                true,
                "not found",
                "Resource not found: " + request.path(),
                null
        );

        return ServerResponse.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(errorResponse));
    }
}