package com.devmind.backend.dto;

public class AIResponse {

    private String response;
    private String provider;

    public AIResponse() {
    }

    public AIResponse(String response, String provider) {
        this.response = response;
        this.provider = provider;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }
}