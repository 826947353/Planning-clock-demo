package com.pomotimer.service;

import com.pomotimer.dto.RecommendationDTO;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class AiSummaryService {

    public List<RecommendationDTO> getRecommendations(String mood) {
        return Arrays.asList(
                RecommendationDTO.builder()
                        .id(1L)
                        .title("听20分钟钢琴")
                        .durationMinutes(20)
                        .category("MUSIC")
                        .description("让舒缓的钢琴曲流淌进你的耳朵，带走疲惫")
                        .icon("piano")
                        .build(),
                RecommendationDTO.builder()
                        .id(2L)
                        .title("花25分钟写一些想法")
                        .durationMinutes(25)
                        .category("WRITING")
                        .description("把心中所想记录下来，清空大脑的缓存")
                        .icon("pen")
                        .build(),
                RecommendationDTO.builder()
                        .id(3L)
                        .title("发呆10分钟")
                        .durationMinutes(10)
                        .category("RELAX")
                        .description("什么都不做，就是最好的充电方式")
                        .icon("cloud")
                        .build(),
                RecommendationDTO.builder()
                        .id(4L)
                        .title("做5分钟拉伸")
                        .durationMinutes(5)
                        .category("SPORT")
                        .description("舒展一下身体，唤醒沉睡的肌肉")
                        .icon("stretch")
                        .build(),
                RecommendationDTO.builder()
                        .id(5L)
                        .title("阅读30分钟")
                        .durationMinutes(30)
                        .category("READING")
                        .description("沉浸在书的世界里，让思绪自由飞驰")
                        .icon("book")
                        .build(),
                RecommendationDTO.builder()
                        .id(6L)
                        .title("冥想15分钟")
                        .durationMinutes(15)
                        .category("MEDITATION")
                        .description("闭上眼睛，专注于一呼一吸之间")
                        .icon("meditation")
                        .build()
        );
    }

    public String generateAiSummary(Long sessionId) {
        return "AI 专注总结接口已预留。请接入 AI API 后，本接口将基于当前专注会话生成个性化总结。\n"
                + "接口路径: POST /api/ai/summary/{sessionId}\n"
                + "需要在 application.yml 中配置 ai.api.url 和 ai.api.key";
    }
}