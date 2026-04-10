package com.microservice.gateway.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceResponse {

	private boolean error;

	private ErrorCode errorcode;

	private String errorMessage;

	private Object data;

	public static ServiceResponse asFailure(ErrorCode code, String errorMessage) {

		return ServiceResponse.builder().data(null).errorcode(code).error(true).errorMessage(errorMessage).build();
	}

	public static ServiceResponse asSuccess(Object data) {

		return ServiceResponse.builder().data(data).error(false).errorcode(null).build();
	}
}