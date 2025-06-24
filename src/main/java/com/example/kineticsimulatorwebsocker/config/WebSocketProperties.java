package com.example.kineticsimulatorwebsocker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "websocket")
public class WebSocketProperties {
    private String endpointPath = "/";

    public String getEndpointPath() {
        return endpointPath;
    }

    public void setEndpointPath(String endpointPath) {
        this.endpointPath = endpointPath;
    }
} 