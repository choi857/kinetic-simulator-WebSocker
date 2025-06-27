package com.example.kineticsimulatorwebsocker.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import java.util.Map;
import java.util.Random;

public class AdvancedDataGenerator {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Random random = new Random();

    // 路径归一化，将所有[数字]替换为[0]
    private static String normalizeArrayPath(String path) {
        return path == null ? null : path.replaceAll("\\[\\d+\\]", "[0]");
    }

    // 类型查找：优先完整路径，查不到再归一化
    private static String getTypeWithFallback(Map<String, String> fieldTypes, String path) {
        String type = fieldTypes.get(path);
        if (type != null) return type;
        String fallbackPath = normalizeArrayPath(path);
        return fieldTypes.getOrDefault(fallbackPath, "string");
    }

    // 限制查找：优先完整路径，查不到再归一化
    private static DynamicWebSocketServer.FieldLimit getFieldLimitWithFallback(Map<String, DynamicWebSocketServer.FieldLimit> fieldLimits, String path) {
        DynamicWebSocketServer.FieldLimit limit = fieldLimits.get(path);
        if (limit != null) return limit;
        String fallbackPath = normalizeArrayPath(path);
        return fieldLimits.get(fallbackPath);
    }

    // 默认值查找：优先完整路径，查不到再归一化
    private static String getDefaultWithFallback(Map<String, String> fieldDefaults, String path) {
        String val = fieldDefaults.get(path);
        if (val != null && !val.isEmpty()) return val;
        String fallbackPath = normalizeArrayPath(path);
        val = fieldDefaults.get(fallbackPath);
        return (val != null && !val.isEmpty()) ? val : null;
    }

    public static JsonNode generate(JsonNode template, Map<String, String> fieldTypes, Map<String, DynamicWebSocketServer.FieldLimit> fieldLimits, Map<String, String> fieldDefaults, int groupCount) {
        return generateRecursive(template, fieldTypes, fieldLimits, fieldDefaults, "", groupCount);
    }

    private static JsonNode generateRecursive(JsonNode template, Map<String, String> fieldTypes, Map<String, DynamicWebSocketServer.FieldLimit> fieldLimits, Map<String, String> fieldDefaults, String path, int groupCount) {
        // 优先用默认值
        String defVal = getDefaultWithFallback(fieldDefaults, path);
        String type = getTypeWithFallback(fieldTypes, path);
        if (defVal != null) {
            return parseDefaultValueToJsonNode(defVal, type, template);
        }
        if (template.isArray()) {
            ArrayNode arr = mapper.createArrayNode();
            int group = template.size();
            int n = group > 0 ? groupCount : 0;
            int len = group * n;
            for (int i = 0; i < len; i++) {
                int groupIdx = i % group;
                String elementPath = path + "[" + i + "]";
                JsonNode elementTemplate = template.get(groupIdx);
                JsonNode elementData = generateRecursive(elementTemplate, fieldTypes, fieldLimits, fieldDefaults, elementPath, 1);
                arr.add(elementData);
            }
            return arr;
        } else if (template.isObject()) {
            ObjectNode obj = mapper.createObjectNode();
            template.fieldNames().forEachRemaining(field -> {
                String fieldPath = path.isEmpty() ? field : path + "." + field;
                JsonNode fieldValue = template.get(field);
                JsonNode generatedValue = generateRecursive(fieldValue, fieldTypes, fieldLimits, fieldDefaults, fieldPath, 1);
                obj.set(field, generatedValue);
            });
            return obj;
        } else {
            return generateRandomByType(type, template, fieldTypes, fieldLimits, fieldDefaults, path);
        }
    }

    private static JsonNode generateRandomByType(String type, JsonNode template, Map<String, String> fieldTypes, Map<String, DynamicWebSocketServer.FieldLimit> fieldLimits, Map<String, String> fieldDefaults, String path) {
        // 优先用默认值
        String defVal = getDefaultWithFallback(fieldDefaults, path);
        if (defVal != null) {
            return parseDefaultValueToJsonNode(defVal, type, template);
        }
        DynamicWebSocketServer.FieldLimit limit = getFieldLimitWithFallback(fieldLimits, path);
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
            case "timestamp_realtime":
                return new LongNode(System.currentTimeMillis());
            case "timestamp_editable": {
                if (defVal != null) {
                    try {
                        return new LongNode(Long.parseLong(defVal));
                    } catch (Exception e) {}
                }
                long min = 1577808000000L;
                long max = System.currentTimeMillis() + 365L * 24 * 3600 * 1000;
                if (limit != null) {
                    try {
                        if (limit.min != null && !limit.min.isEmpty()) min = Long.parseLong(limit.min);
                        if (limit.max != null && !limit.max.isEmpty()) max = Long.parseLong(limit.max);
                    } catch (Exception e) {}
                }
                if (min > max) min = max;
                long val = min + (long)(random.nextDouble() * (max - min + 1));
                return new LongNode(val);
            }
            case "boolean":
                return BooleanNode.valueOf(random.nextBoolean());
            case "array":
                return generateRecursive(template, fieldTypes, fieldLimits, fieldDefaults, path, 1);
            case "object":
                return generateRecursive(template, fieldTypes, fieldLimits, fieldDefaults, path, 1);
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

    // 以下为工具方法，与主服务一致
    private static JsonNode parseDefaultValueToJsonNode(String defVal, String type, JsonNode template) {
        try {
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
                    return new IntNode(Integer.parseInt(defVal));
                case "double":
                case "price":
                case "rate":
                case "score":
                case "temperature":
                case "latitude":
                case "longitude":
                    return new DoubleNode(Double.parseDouble(defVal));
                case "boolean":
                    return BooleanNode.valueOf("true".equalsIgnoreCase(defVal) || "1".equals(defVal));
                case "array":
                    if (defVal.trim().startsWith("[") && defVal.trim().endsWith("]")) {
                        return mapper.readTree(defVal);
                    } else {
                        ArrayNode arr = mapper.createArrayNode();
                        for (String s : defVal.split("\n|,")) {
                            if (!s.trim().isEmpty()) arr.add(s.trim());
                        }
                        return arr;
                    }
                case "object":
                    if (defVal.trim().startsWith("{") && defVal.trim().endsWith("}")) {
                        return mapper.readTree(defVal);
                    } else {
                        if (template != null && template.isObject()) {
                            ObjectNode obj = mapper.createObjectNode();
                            template.fieldNames().forEachRemaining(f -> {
                                obj.set(f, new TextNode(defVal));
                            });
                            return obj;
                        }
                        return new TextNode(defVal);
                    }
                case "date":
                    return new TextNode(defVal);
                default:
                    return new TextNode(defVal);
            }
        } catch (Exception e) {
            return new TextNode(defVal);
        }
    }

    private static JsonNode generateRandomInt(String type, DynamicWebSocketServer.FieldLimit limit) {
        int min = 0, max = 10000;
        if (limit != null && limit.min != null && limit.max != null) {
            try {
                min = Integer.parseInt(limit.min);
                max = Integer.parseInt(limit.max);
            } catch (Exception e) {}
        } else {
            switch (type) {
                case "age": min = 1; max = 100; break;
                case "year": min = 2000; max = 2024; break;
                case "month": min = 1; max = 12; break;
                case "day": min = 1; max = 31; break;
                case "hour": min = 0; max = 23; break;
                case "minute":
                case "second": min = 0; max = 59; break;
                case "port": min = 1024; max = 65535; break;
                case "id": min = 1; max = 1000000; break;
                default: min = 0; max = 10000; break;
            }
        }
        return new IntNode(random.nextInt(max - min + 1) + min);
    }

    private static JsonNode generateRandomDouble(String type, DynamicWebSocketServer.FieldLimit limit) {
        double min = 0.0, max = 100.0;
        if (limit != null && limit.min != null && limit.max != null) {
            try {
                min = Double.parseDouble(limit.min);
                max = Double.parseDouble(limit.max);
            } catch (Exception e) {}
        } else {
            switch (type) {
                case "price": min = 0.0; max = 1000.0; break;
                case "rate": min = 0.0; max = 100.0; break;
                case "score": min = 0.0; max = 10.0; break;
                case "temperature": min = -10.0; max = 40.0; break;
                case "latitude": min = -90.0; max = 90.0; break;
                case "longitude": min = -180.0; max = 180.0; break;
                default: min = 0.0; max = 100.0; break;
            }
        }
        return new DoubleNode(Math.round((random.nextDouble() * (max - min) + min) * 100.0) / 100.0);
    }

    // 其它随机生成工具方法（邮箱、手机号、日期等）可直接复用主服务的静态方法
    private static String generateRandomEmail() {
        String[] domains = {"gmail.com", "yahoo.com", "hotmail.com", "outlook.com", "qq.com", "163.com"};
        String username = randomString(8);
        String domain = domains[random.nextInt(domains.length)];
        return username + "@" + domain;
    }
    private static String generateRandomPhone() {
        String[] prefixes = {"130", "131", "132", "133", "134", "135", "136", "137", "138", "139",
                "150", "151", "152", "153", "155", "156", "157", "158", "159",
                "180", "181", "182", "183", "184", "185", "186", "187", "188", "189"};
        String prefix = prefixes[random.nextInt(prefixes.length)];
        String suffix = String.format("%08d", random.nextInt(100000000));
        return prefix + suffix;
    }
    private static String generateRandomDateTime(DynamicWebSocketServer.FieldLimit limit) {
        // 复用主服务逻辑，简化实现
        return java.time.LocalDateTime.now().toString();
    }
    private static String generateRandomIP() {
        return random.nextInt(256) + "." + random.nextInt(256) + "." + random.nextInt(256) + "." + random.nextInt(256);
    }
    private static String generateRandomURL() {
        String[] protocols = {"http", "https"};
        String[] domains = {"example.com", "test.com", "demo.com", "sample.com"};
        String[] paths = {"api", "data", "user", "product", "order"};
        String protocol = protocols[random.nextInt(protocols.length)];
        String domain = domains[random.nextInt(domains.length)];
        String path = paths[random.nextInt(paths.length)];
        String id = String.valueOf(random.nextInt(1000));
        return protocol + "://" + domain + "/" + path + "/" + id;
    }
    private static String generateRandomUUID() {
        return java.util.UUID.randomUUID().toString();
    }
    private static String generateRandomName() {
        String[] firstNames = {"张", "李", "王", "刘", "陈", "杨", "赵", "黄", "周", "吴"};
        String[] lastNames = {"伟", "芳", "娜", "秀英", "敏", "静", "丽", "强", "磊", "军"};
        String firstName = firstNames[random.nextInt(firstNames.length)];
        String lastName = lastNames[random.nextInt(lastNames.length)];
        return firstName + lastName;
    }
    private static String generateRandomColor() {
        String[] colors = {"red", "blue", "green", "yellow", "purple", "orange", "pink", "brown", "black", "white"};
        return colors[random.nextInt(colors.length)];
    }
    private static String randomString(int len) {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
} 