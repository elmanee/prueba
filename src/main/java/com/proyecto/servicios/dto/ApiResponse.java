package com.proyecto.servicios.dto;

public class ApiResponse<T> {
    private int status;
    private String message;
    private T data;
    private java.time.Instant timestamp = java.time.Instant.now();

    public ApiResponse() {}

    public ApiResponse(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "Operación exitosa", data);
    }

    public static <T> ApiResponse<T> error(int status, String message) {
        return new ApiResponse<>(status, message, null);
    }

    // Getters and setters
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    public java.time.Instant getTimestamp() { return timestamp; }
    public void setTimestamp(java.time.Instant timestamp) { this.timestamp = timestamp; }
}
