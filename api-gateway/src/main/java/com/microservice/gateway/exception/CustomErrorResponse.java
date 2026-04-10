package com.microservice.gateway.exception;

public class CustomErrorResponse {

    private boolean error;
    private String errorcode;
    private String errorMessage;
    private Object data;

    public CustomErrorResponse(boolean error, String errorcode, String errorMessage, Object data) {
        this.error = error;
        this.errorcode = errorcode;
        this.errorMessage = errorMessage;
        this.data = data;
    }

    // Getters and Setters
    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public String getErrorcode() {
        return errorcode;
    }

    public void setErrorcode(String errorcode) {
        this.errorcode = errorcode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}