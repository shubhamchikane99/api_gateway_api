package com.microservice.gateway.exception;

public enum ErrorCode {

	NOT_FOUND("NOT FOUND"), UNAUTHORIZED("UNAUTHORIZED USER"), INTERNAL_SERVER_ERROR("INTERNAL SERVER ERROR"),
	EXPECTATION_FAILED("ENTITY PARAMETER ERROR"), RUNTIME_ERROR("Runtime Exception");

	String message;

	ErrorCode(String message) {
		this.message = message;
	}
}
