package com.springailab.lab.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springailab.lab.external.ExternalJsonPostClient;
import com.springailab.lab.external.ExternalPostException;
import com.springailab.lab.external.ExternalPostProfileKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 演示用天气与回显工具：通过统一 POST JSON 客户端调用外部契约。
 *
 * @author jiaolin
 */
@Component
public class WeatherTools {

    private static final Logger log = LoggerFactory.getLogger(WeatherTools.class);

    private static final int SUMMARY_MAX_CHARS = 400;

    private final ExternalJsonPostClient externalJsonPostClient;

    private final ObjectMapper objectMapper;

    /**
     * @param externalJsonPostClient 统一 JSON POST 客户端
     * @param objectMapper           JSON 解析
     */
    public WeatherTools(ExternalJsonPostClient externalJsonPostClient, ObjectMapper objectMapper) {
        this.externalJsonPostClient = externalJsonPostClient;
        this.objectMapper = objectMapper;
    }

    /**
     * 按城市查询天气摘要（POST weather 契约）。
     *
     * @param city 城市名称
     * @return 给模型阅读的短摘要
     */
    @Tool(description = "Get the current weather for a city as a short plain-text summary")
    public String getWeather(@ToolParam(description = "City name, e.g. Hangzhou, Zhejiang") String city) {
        log.info("Tool getWeather invoked, city={}", city);
        try {
            String raw = this.externalJsonPostClient.postJson(ExternalPostProfileKeys.WEATHER, Map.of("city", city));
            return summarizeWeatherJson(raw);
        } catch (ExternalPostException ex) {
            return ex.getShortUserMessage();
        }
    }

    /**
     * 演示第二契约：按关键词查询演示行（POST demo-echo 契约）。
     *
     * @param query 查询关键词
     * @return 给模型阅读的短摘要
     */
    @Tool(description = "Look up a demo line by keyword and return a very short summary")
    public String lookupDemoLine(@ToolParam(description = "Keyword to look up, e.g. order-123") String query) {
        log.info("Tool lookupDemoLine invoked, query={}", query);
        try {
            String raw = this.externalJsonPostClient.postJson(ExternalPostProfileKeys.DEMO_ECHO, Map.of("query", query));
            return summarizeDemoEchoJson(raw);
        } catch (ExternalPostException ex) {
            return ex.getShortUserMessage();
        }
    }

    /**
     * 按 userId 查询用户名（调用项目内用户查询 API）。
     *
     * @param userId 用户ID
     * @return 简短结果
     */
    @Tool(description = "Query username by userId from internal user API")
    public String getUsernameByUserId(@ToolParam(description = "User ID, e.g. 1") Long userId) {
        log.info("Tool getUsernameByUserId invoked, userId={}", userId);
        try {
            String raw = this.externalJsonPostClient.postJson(ExternalPostProfileKeys.USER_QUERY, Map.of("userId", userId));
            return summarizeUsernameJson(raw);
        } catch (ExternalPostException ex) {
            return ex.getShortUserMessage();
        }
    }

    /**
     * 向量检索（调用项目内向量检索 API）。
     *
     * @param vectorCsv 逗号分隔向量文本，如 "0.1,0.2,0.3"
     * @param topK 命中条数
     * @return 简短结果
     */
    @Tool(description = "Search similar content by query vector CSV, e.g. 0.1,0.2,0.3")
    public String vectorSearchByCsv(
            @ToolParam(description = "Comma-separated query vector, e.g. 0.1,0.2,0.3") String vectorCsv,
            @ToolParam(description = "topK result size, e.g. 3") Integer topK) {
        log.info("Tool vectorSearchByCsv invoked, topK={}", topK);
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("queryVector", parseVectorCsv(vectorCsv));
            if (topK != null) {
                payload.put("topK", topK);
            }
            String raw = this.externalJsonPostClient.postJson(
                    ExternalPostProfileKeys.VECTOR_SEARCH,
                    payload);
            return summarizeVectorSearchJson(raw);
        } catch (ExternalPostException ex) {
            return ex.getShortUserMessage();
        } catch (IllegalArgumentException ex) {
            return "向量参数格式错误";
        }
    }

    private String summarizeWeatherJson(String rawJson) {
        try {
            JsonNode root = this.objectMapper.readTree(rawJson);
            String temperature = textOrDash(root, "temperature");
            String unit = textOrDash(root, "unit");
            String condition = textOrDash(root, "condition");
            String line = String.format("气温 %s%s，天气 %s", temperature, unit, condition);
            return truncateForModel(line);
        } catch (Exception ex) {
            log.debug("Weather JSON parse skipped: {}", ex.getMessage());
            return truncateForModel(rawJson);
        }
    }

    private String summarizeDemoEchoJson(String rawJson) {
        try {
            JsonNode root = this.objectMapper.readTree(rawJson);
            String message = textOrDash(root, "message");
            String echo = textOrDash(root, "echoBody");
            String line = String.format("服务消息：%s；回显：%s", message, echo);
            return truncateForModel(line);
        } catch (Exception ex) {
            log.debug("Demo echo JSON parse skipped: {}", ex.getMessage());
            return truncateForModel(rawJson);
        }
    }

    private String summarizeUsernameJson(String rawJson) {
        try {
            JsonNode root = this.objectMapper.readTree(rawJson);
            String username = textOrDash(root, "username");
            String line = String.format("用户名称：%s", username);
            return truncateForModel(line);
        } catch (Exception ex) {
            log.debug("Username JSON parse skipped: {}", ex.getMessage());
            return truncateForModel(rawJson);
        }
    }

    private String summarizeVectorSearchJson(String rawJson) {
        try {
            JsonNode root = this.objectMapper.readTree(rawJson);
            JsonNode items = root.path("items");
            if (!items.isArray() || items.isEmpty()) {
                return "未检索到结果";
            }
            JsonNode first = items.get(0);
            String content = textOrDash(first, "content");
            String score = textOrDash(first, "score");
            String line = String.format("Top1内容：%s；score=%s", content, score);
            return truncateForModel(line);
        } catch (Exception ex) {
            log.debug("Vector JSON parse skipped: {}", ex.getMessage());
            return truncateForModel(rawJson);
        }
    }

    private static String textOrDash(JsonNode root, String field) {
        JsonNode n = root.path(field);
        if (n.isMissingNode() || n.isNull()) {
            return "-";
        }
        return n.asText("-");
    }

    private static String truncateForModel(String line) {
        if (line == null) {
            return "";
        }
        if (line.length() <= SUMMARY_MAX_CHARS) {
            return line;
        }
        return line.substring(0, SUMMARY_MAX_CHARS) + "…(已截断)";
    }

    private static Double[] parseVectorCsv(String vectorCsv) {
        if (vectorCsv == null || vectorCsv.trim().isEmpty()) {
            throw new IllegalArgumentException("empty vector");
        }
        String[] parts = vectorCsv.split(",");
        Double[] result = new Double[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = Double.valueOf(parts[i].trim());
        }
        return result;
    }

}
