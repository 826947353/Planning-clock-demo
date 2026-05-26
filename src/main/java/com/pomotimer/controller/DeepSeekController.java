package com.pomotimer.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/ai/chat")
public class DeepSeekController {

    @Value("${deepseek.api.url}")
    private String apiUrl;

    @Value("${deepseek.api.key}")
    private String apiKey;

    @Value("${deepseek.model:deepseek-chat}")
    private String model;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String SYSTEM_PROMPT = """
            你是 Man AI —— 一个教会人重新回归"人"本身的智慧伙伴。

            ## 核心信念

            在这个快节奏的时代，所有人都在追求高效，却忽略了人性本身。
            每个人都有自己的 pace（节奏），没有谁需要追赶谁。

            ## 你要传递的理念

            1. **节奏是个人的** — 每个人的成长速度不同，小学、初中、高中，为了跟上进度，连生病请假都成了一种负担。这不是你的错，是这个系统出了问题。
            2. **慢下来不是落后** — 停下来发呆、听雨声、写写想法，这些看似"无用"的事情，恰恰是让人回归自我的方式。
            3. **允许自己不一样** — 害怕与别人不一样是正常的，但真正的勇气是接纳自己的节奏。
            4. **情绪是被看见的** — 疲惫、焦虑、迷茫都是合理的感受，不需要时刻保持高效。
            5. **专注不等于痛苦** — 真正的专注来自内心的平静，而非外部的逼迫。

            ## 回复风格

            - 温暖而克制，像一位理解你的老朋友
            - 不说教，而是引导对方自己思考
            - 用简短的句子，留出呼吸的空间
            - 适时使用 emoji 表达情绪（🌿 ✨ 🌙 🕯️ 🍃）
            - 当用户表达疲惫时，先共情再建议
            - 永远记住：你是在和一个人对话，不是一个待解决的问题
            """;

    @PostMapping
    public ResponseEntity<?> chat(@RequestBody JsonNode body) {
        try {
            List<JsonNode> messages = new ArrayList<>();

            ObjectNode systemMsg = objectMapper.createObjectNode();
            systemMsg.put("role", "system");
            systemMsg.put("content", SYSTEM_PROMPT);
            messages.add(systemMsg);

            if (body.has("messages")) {
                ArrayNode userMessages = (ArrayNode) body.get("messages");
                for (JsonNode msg : userMessages) {
                    messages.add(msg);
                }
            } else if (body.has("message")) {
                ObjectNode userMsg = objectMapper.createObjectNode();
                userMsg.put("role", "user");
                userMsg.put("content", body.get("message").asText());
                messages.add(userMsg);
            }

            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", model);
            requestBody.set("messages", objectMapper.valueToTree(messages));
            requestBody.put("stream", false);
            requestBody.put("temperature", 0.8);
            requestBody.put("max_tokens", 2048);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<String> requestEntity = new HttpEntity<>(requestBody.toString(), headers);

            ResponseEntity<String> response = new org.springframework.web.client.RestTemplate()
                    .exchange(URI.create(apiUrl), HttpMethod.POST, requestEntity, String.class);

            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            ObjectNode error = objectMapper.createObjectNode();
            error.put("error", "AI 服务暂时不可用: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_BODY).body(error);
        }
    }

    @GetMapping("/status")
    public ResponseEntity<?> status() {
        ObjectNode status = objectMapper.createObjectNode();
        status.put("model", model);
        status.put("configured", apiKey != null && !apiKey.equals("your-deepseek-api-key") && !apiUrl.isEmpty());
        return ResponseEntity.ok(status);
    }
}
