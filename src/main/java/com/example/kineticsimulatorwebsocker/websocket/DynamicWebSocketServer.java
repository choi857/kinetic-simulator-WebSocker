package com.example.kineticsimulatorwebsocker.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Random;

/**
 * 支持动态json结构的WebSocket服务端
 */
@ServerEndpoint("/")
@Component
public class DynamicWebSocketServer {
    private static final Logger logger = LoggerFactory.getLogger(DynamicWebSocketServer.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Map<Session, TemplateConfig> sessionTemplateMap = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private static final Random random = new Random();
    // 全局最新模板配置
    private static volatile TemplateConfig latestGlobalTemplate = null;

    private Session session;
    
    // 模板配置类
    public static class TemplateConfig {
        public JsonNode template; // 原始模板
        public Map<String, String> fieldTypes; // 字段类型映射
        public Map<String, FieldLimit> fieldLimits; // 字段限制映射

        public TemplateConfig(JsonNode template, Map<String, String> fieldTypes) {
            this.template = template;
            this.fieldTypes = fieldTypes;
            this.fieldLimits = new ConcurrentHashMap<>();
        }
        
        public TemplateConfig(JsonNode template, Map<String, String> fieldTypes, Map<String, FieldLimit> fieldLimits) {
            this.template = template;
            this.fieldTypes = fieldTypes;
            this.fieldLimits = fieldLimits != null ? fieldLimits : new ConcurrentHashMap<>();
        }
    }

    // 字段限制类
    public static class FieldLimit {
        public String min;
        public String max;
        
        public FieldLimit(String min, String max) {
            this.min = min;
            this.max = max;
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        logger.info("新连接: {}，等待3秒内收到模板，否则自动使用全局模板", session.getId());
        // 3秒后如果还没收到模板，自动分配全局模板
        scheduler.schedule(() -> {
            if (session.isOpen() && !sessionTemplateMap.containsKey(session)) {
                if (latestGlobalTemplate != null) {
                    sessionTemplateMap.put(session, latestGlobalTemplate);
                    logger.info("连接{}未收到模板，自动分配全局模板", session.getId());
                    sendRandomData(session, latestGlobalTemplate);
                    scheduler.scheduleAtFixedRate(() -> {
                        if (session.isOpen()) {
                            sendRandomData(session, latestGlobalTemplate);
                        }
                    }, 5, 5, TimeUnit.SECONDS);
                } else {
                    logger.warn("连接{}未收到模板，且全局模板为空，无法推送数据", session.getId());
                    sendText(session, "未收到模板且全局模板为空，无法推送数据");
                }
            }
        }, 3, TimeUnit.SECONDS);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        if (!sessionTemplateMap.containsKey(session)) {
            // 只在第一次收到消息时尝试解析为模板
            try {
                JsonNode jsonNode = mapper.readTree(message);
                
                // 检查是否是包含类型配置的模板
                if (jsonNode.has("template") && jsonNode.has("fieldTypes")) {
                    // 新的格式：包含模板和字段类型配置
                    JsonNode template = jsonNode.get("template");
                    JsonNode fieldTypesNode = jsonNode.get("fieldTypes");
                    
                    Map<String, String> fieldTypes = new ConcurrentHashMap<>();
                    fieldTypesNode.fieldNames().forEachRemaining(field -> {
                        fieldTypes.put(field, fieldTypesNode.get(field).asText());
                    });
                    
                    // 处理字段限制
                    Map<String, FieldLimit> fieldLimits = new ConcurrentHashMap<>();
                    if (jsonNode.has("fieldLimits")) {
                        JsonNode fieldLimitsNode = jsonNode.get("fieldLimits");
                        fieldLimitsNode.fieldNames().forEachRemaining(field -> {
                            JsonNode limitNode = fieldLimitsNode.get(field);
                            String min = limitNode.has("min") ? limitNode.get("min").asText() : null;
                            String max = limitNode.has("max") ? limitNode.get("max").asText() : null;
                            if (min != null || max != null) {
                                fieldLimits.put(field, new FieldLimit(min, max));
                            }
                        });
                    }
                    
                    TemplateConfig config = new TemplateConfig(template, fieldTypes, fieldLimits);
                    sessionTemplateMap.put(session, config);
                    latestGlobalTemplate = config; // 更新全局模板
                    logger.info("收到并保存模板配置: 模板={}, 字段类型={}, 字段限制={}", template.toString(), fieldTypes, fieldLimits);
                    
                    // 立即推送一次
                    sendRandomData(session, config);
                    // 启动定时推送
                    scheduler.scheduleAtFixedRate(() -> {
                        if (session.isOpen()) {
                            sendRandomData(session, config);
                        }
                    }, 5, 5, TimeUnit.SECONDS);
                } else {
                    // 旧格式：直接是JSON模板，使用字段名推断类型
                    Map<String, String> fieldTypes = inferFieldTypes(jsonNode);
                    TemplateConfig config = new TemplateConfig(jsonNode, fieldTypes);
                    sessionTemplateMap.put(session, config);
                    latestGlobalTemplate = config;
                    logger.info("收到并保存模板: {}，已推断字段类型: {}", jsonNode.toString(), fieldTypes);
                    
                    // 立即推送一次
                    sendRandomData(session, config);
                    // 启动定时推送
                    scheduler.scheduleAtFixedRate(() -> {
                        if (session.isOpen()) {
                            sendRandomData(session, config);
                        }
                    }, 5, 5, TimeUnit.SECONDS);
                }
            } catch (Exception e) {
                logger.warn("解析json模板失败: {}", e.getMessage());
                sendText(session, "模板解析失败: " + e.getMessage());
            }
        } else {
            // 后续消息只做简单回显或忽略
            String msg = message.trim();
            if ("HEARTBEAT".equalsIgnoreCase(msg) || "PING".equalsIgnoreCase(msg)) {
                // 忽略心跳包
                return;
            }
            // 可选：尝试解析为JSON，仅回显解析成功的内容，否则只回显原文
            try {
                JsonNode node = mapper.readTree(message);
                sendText(session, "已收到JSON: " + node.toString());
            } catch (Exception e) {
                sendText(session, "已收到: " + message);
            }
        }
    }

    @OnClose
    public void onClose(Session session) {
        sessionTemplateMap.remove(session);
        logger.info("连接关闭: {}", session.getId());
    }

    @OnError
    public void onError(Session session, Throwable error) {
        logger.error("发生错误: {}", error.getMessage());
    }

    private void sendText(Session session, String text) {
        try {
            if (session.isOpen()) session.getBasicRemote().sendText(text);
        } catch (IOException e) {
            logger.warn("发送消息失败: {}", e.getMessage());
        }
    }

    private void sendRandomData(Session session, TemplateConfig config) {
        try {
            JsonNode data = generateRandomByTemplate(config);
            String json = mapper.writeValueAsString(data);
            sendText(session, json);
        } catch (Exception e) {
            logger.warn("生成或发送随机数据失败: {}", e.getMessage());
        }
    }

    // 根据字段名推断类型
    private Map<String, String> inferFieldTypes(JsonNode template) {
        Map<String, String> fieldTypes = new ConcurrentHashMap<>();
        inferFieldTypesRecursive(template, "", fieldTypes);
        return fieldTypes;
    }

    private void inferFieldTypesRecursive(JsonNode node, String path, Map<String, String> fieldTypes) {
        if (node.isArray()) {
            // 处理数组类型
            if (node.size() > 0) {
                // 为数组的第一个元素推断字段类型
                inferFieldTypesRecursive(node.get(0), path + "[0]", fieldTypes);
            }
        } else if (node.isObject()) {
            node.fieldNames().forEachRemaining(field -> {
                String fieldPath = path.isEmpty() ? field : path + "." + field;
                JsonNode fieldValue = node.get(field);
                fieldTypes.put(fieldPath, inferTypeByFieldName(field, fieldValue));
                if (fieldValue.isObject() || fieldValue.isArray()) {
                    inferFieldTypesRecursive(fieldValue, fieldPath, fieldTypes);
                }
            });
        }
    }

    private String inferTypeByFieldName(String fieldName, JsonNode value) {
        String lowerFieldName = fieldName.toLowerCase();
        
        // 根据字段名推断类型
        if (lowerFieldName.contains("_int") || lowerFieldName.contains("_integer") || 
            lowerFieldName.contains("id") || lowerFieldName.contains("count") || 
            lowerFieldName.contains("number") || lowerFieldName.contains("index") ||
            lowerFieldName.contains("type")) {
            return "int";
        }
        
        if (lowerFieldName.contains("_double") || lowerFieldName.contains("_float") || 
            lowerFieldName.contains("price") || lowerFieldName.contains("rate") || 
            lowerFieldName.contains("score") || lowerFieldName.contains("value") ||
            lowerFieldName.contains("x") || lowerFieldName.contains("y") || lowerFieldName.contains("z") ||
            lowerFieldName.contains("a") || lowerFieldName.contains("d")) {
            return "double";
        }
        
        if (lowerFieldName.contains("_bool") || lowerFieldName.contains("_boolean") || 
            lowerFieldName.contains("is") || lowerFieldName.contains("has") || 
            lowerFieldName.contains("enable") || lowerFieldName.contains("active")) {
            return "boolean";
        }
        
        if (lowerFieldName.contains("email")) return "email";
        if (lowerFieldName.contains("phone") || lowerFieldName.contains("mobile")) return "phone";
        if (lowerFieldName.contains("date") || lowerFieldName.contains("time")) return "date";
        if (lowerFieldName.contains("ip")) return "ip";
        if (lowerFieldName.contains("url")) return "url";
        if (lowerFieldName.contains("uuid") || lowerFieldName.contains("guid")) return "uuid";
        if (lowerFieldName.contains("name")) return "name";
        if (lowerFieldName.contains("color")) return "color";
        if (lowerFieldName.contains("age")) return "age";
        if (lowerFieldName.contains("year")) return "year";
        if (lowerFieldName.contains("month")) return "month";
        if (lowerFieldName.contains("day")) return "day";
        if (lowerFieldName.contains("hour")) return "hour";
        if (lowerFieldName.contains("minute")) return "minute";
        if (lowerFieldName.contains("second")) return "second";
        if (lowerFieldName.contains("port")) return "port";
        if (lowerFieldName.contains("temperature")) return "temperature";
        if (lowerFieldName.contains("latitude")) return "latitude";
        if (lowerFieldName.contains("longitude")) return "longitude";
        
        // 根据值类型推断
        if (value.isArray()) return "array";
        if (value.isObject()) return "object";
        if (value.isTextual()) return "string";
        if (value.isInt() || value.isLong()) return "int";
        if (value.isDouble() || value.isFloat()) return "double";
        if (value.isBoolean()) return "boolean";
        
        return "string"; // 默认字符串
    }

    // 递归生成与模板结构一致的随机数据
    private JsonNode generateRandomByTemplate(TemplateConfig config) {
        return generateRandomByTemplateRecursive(config.template, config.fieldTypes, "", config);
    }

    private JsonNode generateRandomByTemplateRecursive(JsonNode template, Map<String, String> fieldTypes, String path, TemplateConfig config) {
        if (template.isArray()) {
            ArrayNode arr = mapper.createArrayNode();
            // 生成1-3个元素
            int len = template.size() > 0 ? random.nextInt(3) + 1 : 0;
            for (int i = 0; i < len; i++) {
                // 为数组中的每个元素生成数据
                JsonNode elementData = generateRandomByTemplateRecursive(template.get(0), fieldTypes, path + "[" + i + "]", config);
                arr.add(elementData);
            }
            return arr;
        } else if (template.isObject()) {
            ObjectNode obj = mapper.createObjectNode();
            template.fieldNames().forEachRemaining(field -> {
                String fieldPath = path.isEmpty() ? field : path + "." + field;
                JsonNode fieldValue = template.get(field);
                String fieldType = fieldTypes.getOrDefault(fieldPath, "string");
                JsonNode generatedValue = generateRandomByType(fieldType, fieldValue, fieldTypes, fieldPath, config);
                obj.set(field, generatedValue);
            });
            return obj;
        } else {
            String fieldType = fieldTypes.getOrDefault(path, "string");
            return generateRandomByType(fieldType, template, fieldTypes, path, config);
        }
    }

    // 根据指定类型生成随机数据
    private JsonNode generateRandomByType(String type, JsonNode template, Map<String, String> fieldTypes, String path, TemplateConfig config) {
        // 获取字段限制
        FieldLimit limit = config.fieldLimits.get(path);
        
        switch (type) {
            case "int":
            case "age":
            case "year":
            case "month":
            case "day":
            case "hour":
            case "minute":
            case "second":
            case "port":
            case "id":
                return generateRandomInt(type, limit);
            case "double":
            case "price":
            case "rate":
            case "score":
            case "temperature":
            case "latitude":
            case "longitude":
                return generateRandomDouble(type, limit);
            case "boolean":
                return BooleanNode.valueOf(random.nextBoolean());
            case "array":
                return generateRandomArray(template, fieldTypes, path, config);
            case "object":
                return generateRandomObject(template, fieldTypes, path, config);
            case "email":
                return new TextNode(generateRandomEmail());
            case "phone":
                return new TextNode(generateRandomPhone());
            case "date":
                return new TextNode(generateRandomDateTime(limit));
            case "ip":
                return new TextNode(generateRandomIP());
            case "url":
                return new TextNode(generateRandomURL());
            case "uuid":
                return new TextNode(generateRandomUUID());
            case "name":
                return new TextNode(generateRandomName());
            case "color":
                return new TextNode(generateRandomColor());
            case "string":
            default:
                return new TextNode(randomString(6));
        }
    }

    // 生成随机整数
    private JsonNode generateRandomInt(String type, FieldLimit limit) {
        int min = 0;
        int max = 10000; // 默认最大值
        
        if (limit != null && limit.min != null && limit.max != null) {
            try {
                min = Integer.parseInt(limit.min);
                max = Integer.parseInt(limit.max);
            } catch (NumberFormatException e) {
                // 如果解析失败，使用默认值
                logger.warn("解析字段限制失败，使用默认值: min={}, max={}", limit.min, limit.max);
            }
        } else {
            // 没有限制时使用默认范围
            switch (type) {
                case "age":
                    min = 1; max = 100; break;
                case "year":
                    min = 2000; max = 2024; break;
                case "month":
                    min = 1; max = 12; break;
                case "day":
                    min = 1; max = 31; break;
                case "hour":
                    min = 0; max = 23; break;
                case "minute":
                case "second":
                    min = 0; max = 59; break;
                case "port":
                    min = 1024; max = 65535; break;
                case "id":
                    min = 1; max = 1000000; break;
                default:
                    min = 0; max = 10000; break;
            }
        }
        
        return new IntNode(random.nextInt(max - min + 1) + min);
    }

    // 生成随机浮点数
    private JsonNode generateRandomDouble(String type, FieldLimit limit) {
        double min = 0.0;
        double max = 100.0; // 默认最大值
        
        if (limit != null && limit.min != null && limit.max != null) {
            try {
                min = Double.parseDouble(limit.min);
                max = Double.parseDouble(limit.max);
            } catch (NumberFormatException e) {
                // 如果解析失败，使用默认值
                logger.warn("解析字段限制失败，使用默认值: min={}, max={}", limit.min, limit.max);
            }
        } else {
            // 没有限制时使用默认范围
            switch (type) {
                case "price":
                    min = 0.0; max = 1000.0; break;
                case "rate":
                    min = 0.0; max = 100.0; break;
                case "score":
                    min = 0.0; max = 10.0; break;
                case "temperature":
                    min = -10.0; max = 40.0; break;
                case "latitude":
                    min = -90.0; max = 90.0; break;
                case "longitude":
                    min = -180.0; max = 180.0; break;
                default:
                    min = 0.0; max = 100.0; break;
            }
        }
        
        switch (type) {
            case "price":
                return new DoubleNode(Math.round((random.nextDouble() * (max - min) + min) * 100.0) / 100.0);
            case "rate":
                return new DoubleNode(Math.round((random.nextDouble() * (max - min) + min) * 100.0) / 100.0);
            case "score":
                return new DoubleNode(Math.round((random.nextDouble() * (max - min) + min) * 10.0) / 10.0);
            case "temperature":
                return new DoubleNode(Math.round((random.nextDouble() * (max - min) + min) * 10.0) / 10.0);
            case "latitude":
                return new DoubleNode(Math.round((random.nextDouble() * (max - min) + min) * 1000000.0) / 1000000.0);
            case "longitude":
                return new DoubleNode(Math.round((random.nextDouble() * (max - min) + min) * 1000000.0) / 1000000.0);
            default:
                return new DoubleNode(Math.round((random.nextDouble() * (max - min) + min) * 100.0) / 100.0);
        }
    }

    // 生成随机邮箱
    private String generateRandomEmail() {
        String[] domains = {"gmail.com", "yahoo.com", "hotmail.com", "outlook.com", "qq.com", "163.com"};
        String username = randomString(8);
        String domain = domains[random.nextInt(domains.length)];
        return username + "@" + domain;
    }

    // 生成随机手机号
    private String generateRandomPhone() {
        String[] prefixes = {"130", "131", "132", "133", "134", "135", "136", "137", "138", "139",
                           "150", "151", "152", "153", "155", "156", "157", "158", "159",
                           "180", "181", "182", "183", "184", "185", "186", "187", "188", "189"};
        String prefix = prefixes[random.nextInt(prefixes.length)];
        String suffix = String.format("%08d", random.nextInt(100000000));
        return prefix + suffix;
    }

    // 生成随机日期时间
    private String generateRandomDateTime(FieldLimit limit) {
        int year = 2020 + random.nextInt(5);
        int month = random.nextInt(12) + 1;
        int day = random.nextInt(28) + 1;
        int hour = random.nextInt(24);
        int minute = random.nextInt(60);
        int second = random.nextInt(60);
        
        // 如果有日期限制，则应用限制
        if (limit != null && limit.min != null) {
            try {
                String[] parts = limit.min.split(" ");
                if (parts.length == 2) {
                    String[] dateParts = parts[0].split("-");
                    String[] timeParts = parts[1].split(":");
                    
                    if (dateParts.length == 3 && timeParts.length == 3) {
                        int yearLimit = Integer.parseInt(dateParts[0]);
                        int monthLimit = Integer.parseInt(dateParts[1]);
                        int dayLimit = Integer.parseInt(dateParts[2]);
                        int hourLimit = Integer.parseInt(timeParts[0]);
                        int minuteLimit = Integer.parseInt(timeParts[1]);
                        int secondLimit = Integer.parseInt(timeParts[2]);
                        
                        // 确保生成的日期不小于限制的最小值
                        if (year < yearLimit || 
                            (year == yearLimit && month < monthLimit) ||
                            (year == yearLimit && month == monthLimit && day < dayLimit) ||
                            (year == yearLimit && month == monthLimit && day == dayLimit && hour < hourLimit) ||
                            (year == yearLimit && month == monthLimit && day == dayLimit && hour == hourLimit && minute < minuteLimit) ||
                            (year == yearLimit && month == monthLimit && day == dayLimit && hour == hourLimit && minute == minuteLimit && second < secondLimit)) {
                            
                            year = yearLimit;
                            month = monthLimit;
                            day = dayLimit;
                            hour = hourLimit;
                            minute = minuteLimit;
                            second = secondLimit;
                        }
                    }
                }
            } catch (Exception e) {
                logger.warn("解析日期限制失败，使用默认值: {}", limit.min);
            }
        }
        
        return String.format("%04d-%02d-%02d %02d:%02d:%02d", year, month, day, hour, minute, second);
    }

    // 生成随机IP地址
    private String generateRandomIP() {
        return random.nextInt(256) + "." + random.nextInt(256) + "." + 
               random.nextInt(256) + "." + random.nextInt(256);
    }

    // 生成随机URL
    private String generateRandomURL() {
        String[] protocols = {"http", "https"};
        String[] domains = {"example.com", "test.com", "demo.com", "sample.com"};
        String[] paths = {"api", "data", "user", "product", "order"};
        
        String protocol = protocols[random.nextInt(protocols.length)];
        String domain = domains[random.nextInt(domains.length)];
        String path = paths[random.nextInt(paths.length)];
        String id = String.valueOf(random.nextInt(1000));
        
        return protocol + "://" + domain + "/" + path + "/" + id;
    }

    // 生成随机UUID
    private String generateRandomUUID() {
        return java.util.UUID.randomUUID().toString();
    }

    // 生成随机姓名
    private String generateRandomName() {
        String[] firstNames = {"张", "李", "王", "刘", "陈", "杨", "赵", "黄", "周", "吴"};
        String[] lastNames = {"伟", "芳", "娜", "秀英", "敏", "静", "丽", "强", "磊", "军"};
        
        String firstName = firstNames[random.nextInt(firstNames.length)];
        String lastName = lastNames[random.nextInt(lastNames.length)];
        return firstName + lastName;
    }

    // 生成随机颜色
    private String generateRandomColor() {
        String[] colors = {"red", "blue", "green", "yellow", "purple", "orange", "pink", "brown", "black", "white"};
        return colors[random.nextInt(colors.length)];
    }

    private String randomString(int len) {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    // 生成随机数组
    private JsonNode generateRandomArray(JsonNode template, Map<String, String> fieldTypes, String path, TemplateConfig config) {
        ArrayNode arr = mapper.createArrayNode();
        // 生成1-3个元素
        int len = random.nextInt(3) + 1;
        for (int i = 0; i < len; i++) {
            if (template.size() > 0) {
                JsonNode elementData = generateRandomByTemplateRecursive(template.get(0), fieldTypes, path + "[" + i + "]", config);
                arr.add(elementData);
            }
        }
        return arr;
    }

    // 生成随机对象
    private JsonNode generateRandomObject(JsonNode template, Map<String, String> fieldTypes, String path, TemplateConfig config) {
        ObjectNode obj = mapper.createObjectNode();
        if (template.isObject()) {
            template.fieldNames().forEachRemaining(field -> {
                String fieldPath = path.isEmpty() ? field : path + "." + field;
                JsonNode fieldValue = template.get(field);
                String fieldType = fieldTypes.getOrDefault(fieldPath, "string");
                JsonNode generatedValue = generateRandomByType(fieldType, fieldValue, fieldTypes, fieldPath, config);
                obj.set(field, generatedValue);
            });
        }
        return obj;
    }

    public static int getOnlineCount() {
        return sessionTemplateMap.size();
    }
} 