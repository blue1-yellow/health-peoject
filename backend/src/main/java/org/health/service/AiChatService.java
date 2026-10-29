package org.health.service;

import org.health.pojo.AiChatResponse;
import org.springframework.web.multipart.MultipartFile;

public interface AiChatService {
    AiChatResponse chat(Integer userId, String message);
    AiChatResponse dailyReport(Integer userId);
    AiChatResponse identifyFood(Integer userId, MultipartFile image);
    AiChatResponse identifyWorkout(Integer userId, MultipartFile image);
}
