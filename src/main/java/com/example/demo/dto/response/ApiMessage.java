package com.example.demo.dto.response;

public class ApiMessage {
    private String message;

    public ApiMessage() {}
    public ApiMessage(String message) { this.message = message; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
