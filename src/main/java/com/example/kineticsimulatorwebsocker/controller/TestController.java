package com.example.kineticsimulatorwebsocker.controller;

import com.example.kineticsimulatorwebsocker.websocket.KineticWebSocketServer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * 测试控制器
 * 提供手动触发推送的接口
 */
@RestController
@RequestMapping("/api/test")
public class TestController {

    /**
     * 手动触发推送动力学数据
     */
    @GetMapping("/push")
    public String pushKineticData() {
        try {
            KineticWebSocketServer.sendKineticDataToAll();
            return "推送成功，当前在线人数: " + KineticWebSocketServer.getOnlineCount();
        } catch (Exception e) {
            return "推送失败: " + e.getMessage();
        }
    }

    /**
     * 获取当前在线人数
     */
    @GetMapping("/online-count")
    public String getOnlineCount() {
        return "当前在线人数: " + KineticWebSocketServer.getOnlineCount();
    }

    /**
     * 发送自定义消息
     */
    @GetMapping("/send-message")
    public String sendMessage() {
        try {
            KineticWebSocketServer.sendInfo("这是一条测试消息");
            return "消息发送成功";
        } catch (IOException e) {
            return "消息发送失败: " + e.getMessage();
        }
    }
} 