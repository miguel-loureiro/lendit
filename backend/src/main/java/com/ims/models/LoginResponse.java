package com.ims.models;

public class LoginResponse {
    private String token;
    private String error;

    public String getToken() {
        return token;
    }

    public LoginResponse setToken(String token) {
        if (token == null) {
            throw new IllegalArgumentException("Token cannot be null");
        }
        this.token = token;
        return this;
    }

    public String getError() {
        return error;
    }

    public LoginResponse setError(String error) {
        if (error == null) {
            throw new IllegalArgumentException("Error cannot be null");
        }
        this.error = error;
        return this;
    }
}

