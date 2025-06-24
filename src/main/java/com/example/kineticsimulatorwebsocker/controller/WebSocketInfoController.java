package com.example.kineticsimulatorwebsocker.controller;

import com.example.kineticsimulatorwebsocker.config.WebSocketProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class WebSocketInfoController {
    
    @Autowired
    private WebSocketProperties webSocketProperties;
    
    @Value("${server.port:1883}")
    private int serverPort;
    
    @GetMapping("/api/websocket-info")
    public Map<String, Object> getWebSocketInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("port", serverPort);
        info.put("path", webSocketProperties.getEndpointPath());
        info.put("url", "ws://localhost:" + serverPort + webSocketProperties.getEndpointPath());
        return info;
    }
} 