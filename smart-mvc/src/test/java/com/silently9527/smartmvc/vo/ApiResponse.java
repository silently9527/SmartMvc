package com.silently9527.smartmvc.vo;

public class ApiResponse {
    private int code;
    private String message;
    private String data;

    public ApiResponse(int code, String message, String data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getData() {
        return data;
    }
}
