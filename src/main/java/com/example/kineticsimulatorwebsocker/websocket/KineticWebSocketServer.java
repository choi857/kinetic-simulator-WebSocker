package com.example.kineticsimulatorwebsocker.websocket;

import com.example.kineticsimulatorwebsocker.model.KineticData;
import com.example.kineticsimulatorwebsocker.model.MessageResponse;
import com.example.kineticsimulatorwebsocker.service.KineticDataService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * WebSocket服务类
 * 实现连接管理和消息推送功能
 */
@ServerEndpoint("/websocket/kinetic")
@Component
public class KineticWebSocketServer {

    private static final Logger logger = LoggerFactory.getLogger(KineticWebSocketServer.class);

    /**
     * 用来记录当前在线连接数
     */
    private static AtomicInteger onlineCount = new AtomicInteger(0);

    /**
     * 用来存放每个客户端对应的WebSocketServer对象
     */
    private static CopyOnWriteArraySet<KineticWebSocketServer> webSocketSet = new CopyOnWriteArraySet<>();

    /**
     * 与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    private Session session;

    /**
     * 注入动力学数据服务
     * 注意：由于WebSocket是单例，需要通过ApplicationContext来获取Bean
     */
    private static KineticDataService kineticDataService;

    @Autowired
    public void setKineticDataService(KineticDataService kineticDataService) {
        KineticWebSocketServer.kineticDataService = kineticDataService;
    }

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        webSocketSet.add(this);
        addOnlineCount();
        logger.info("有新连接加入！当前在线人数为{}", getOnlineCount());
        
        // 连接建立后立即推送示例数据
        sendKineticData();
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        webSocketSet.remove(this);
        subOnlineCount();
        logger.info("有一连接关闭！当前在线人数为{}", getOnlineCount());
    }

    /**
     * 收到客户端消息后调用的方法
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        logger.info("来自客户端的消息: {}", message);
        
        // 这里可以根据客户端消息进行相应的处理
        // 目前简单回复一个确认消息
        try {
            sendMessage("服务器已收到消息: " + message);
        } catch (IOException e) {
            logger.error("发送消息失败", e);
        }
    }

    /**
     * 发生错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error) {
        logger.error("发生错误", error);
    }

    /**
     * 发送消息给客户端
     */
    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }

    /**
     * 推送动力学数据
     */
    public void sendKineticData() {
        try {
            // 使用服务层生成随机数据
            List<KineticData> dataList = null;
            if (kineticDataService != null) {
                dataList = kineticDataService.generateKineticData();
            } else {
                // 如果服务未注入，使用默认数据
                dataList = getDefaultData();
            }
            
            MessageResponse response = new MessageResponse(dataList);
            ObjectMapper mapper = new ObjectMapper();
            String jsonMessage = mapper.writeValueAsString(response);
            
            sendMessage(jsonMessage);
            logger.info("推送动力学数据: {}", jsonMessage);
        } catch (JsonProcessingException e) {
            logger.error("JSON序列化失败", e);
        } catch (IOException e) {
            logger.error("发送动力学数据失败", e);
        }
    }

    /**
     * 获取默认数据（备用方案）
     */
    private List<KineticData> getDefaultData() {
        List<KineticData> dataList = new java.util.ArrayList<>();
        dataList.add(new KineticData("2522", "1", "-1.09", "12.83", "0.00", "-4.85", "12.88", "0.00", "0.26"));
        dataList.add(new KineticData("2523", "1", "-1.28", "48.76", "0.00", "-1.50", "48.78", "0.00", "-1.17"));
        return dataList;
    }

    /**
     * 群发自定义消息
     */
    public static void sendInfo(String message) throws IOException {
        logger.info(message);
        for (KineticWebSocketServer item : webSocketSet) {
            try {
                item.sendMessage(message);
            } catch (IOException e) {
                continue;
            }
        }
    }

    /**
     * 群发动力学数据
     */
    public static void sendKineticDataToAll() {
        for (KineticWebSocketServer item : webSocketSet) {
            try {
                item.sendKineticData();
            } catch (Exception e) {
                logger.error("群发动力学数据失败", e);
            }
        }
    }

    public static synchronized int getOnlineCount() {
        return onlineCount.get();
    }

    public static synchronized void addOnlineCount() {
        KineticWebSocketServer.onlineCount.incrementAndGet();
    }

    public static synchronized void subOnlineCount() {
        KineticWebSocketServer.onlineCount.decrementAndGet();
    }
} 