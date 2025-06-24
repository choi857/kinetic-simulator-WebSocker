package com.example.kineticsimulatorwebsocker.model;

import java.util.List;

/**
 * 消息响应类
 * 用于封装推送的数据格式
 */
public class MessageResponse {
    private List<KineticData> data;

    public MessageResponse() {}

    public MessageResponse(List<KineticData> data) {
        this.data = data;
    }

    public List<KineticData> getData() {
        return data;
    }

    public void setData(List<KineticData> data) {
        this.data = data;
    }
} 