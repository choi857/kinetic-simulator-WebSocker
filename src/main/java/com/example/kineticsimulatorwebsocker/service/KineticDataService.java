package com.example.kineticsimulatorwebsocker.service;

import com.example.kineticsimulatorwebsocker.model.KineticData;
import com.example.kineticsimulatorwebsocker.websocket.KineticWebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 动力学数据服务类
 * 用于生成模拟数据和定时推送
 */
@Service
public class KineticDataService {

    private static final Logger logger = LoggerFactory.getLogger(KineticDataService.class);
    private final Random random = new Random();

    /**
     * 生成模拟的雷达数据
     */
    public List<KineticData> generateKineticData() {
        List<KineticData> dataList = new ArrayList<>();
        
        // 生成2-5个随机数据项
        int count = random.nextInt(4) + 2;
        
        for (int i = 0; i < count; i++) {
            String id = String.valueOf(2500 + random.nextInt(100));
            String type = String.valueOf(random.nextInt(3) + 1);
            
            // 生成随机坐标和速度
            String x = String.format("%.2f", (random.nextDouble() - 0.5) * 10);
            String y = String.format("%.2f", random.nextDouble() * 100);
            String z = String.format("%.2f", (random.nextDouble() - 0.5) * 5);
            String a = String.format("%.2f", (random.nextDouble() - 0.5) * 10);
            String d = String.format("%.2f", random.nextDouble() * 50);
            String vx = String.format("%.2f", (random.nextDouble() - 0.5) * 2);
            String vy = String.format("%.2f", (random.nextDouble() - 0.5) * 2);
            
            KineticData data = new KineticData(id, type, x, y, z, a, d, vx, vy);
            dataList.add(data);
        }
        
        return dataList;
    }

    /**
     * 定时推送数据（每5秒推送一次）
     * 可以通过配置文件或启动参数来控制是否启用
     */
    @Scheduled(fixedRate = 5000)
    public void scheduledPush() {
        // 只有在有客户端连接时才推送
        if (KineticWebSocketServer.getOnlineCount() > 0) {
            logger.info("定时推送动力学数据，当前在线客户端数: {}", KineticWebSocketServer.getOnlineCount());
            KineticWebSocketServer.sendKineticDataToAll();
        }
    }

    /**
     * 手动推送指定数据
     */
    public void pushCustomData(List<KineticData> dataList) {
        if (dataList != null && !dataList.isEmpty()) {
            KineticWebSocketServer.sendKineticDataToAll();
            logger.info("手动推送自定义数据，数据条数: {}", dataList.size());
        }
    }

    /**
     * 获取当前在线客户端数量
     */
    public int getOnlineClientCount() {
        return KineticWebSocketServer.getOnlineCount();
    }
} 