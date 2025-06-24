package com.example.kineticsimulatorwebsocker.controller;

import com.example.kineticsimulatorwebsocker.websocket.DynamicWebSocketServer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
public class DynamicOnlineCountController {
    @GetMapping("/api/connection-count")
    public Map<String, Object> getConnectionCount() {
        Map<String, Object> response = new HashMap<>();
        response.put("count", DynamicWebSocketServer.getOnlineCount());
        return response;
    }
} 