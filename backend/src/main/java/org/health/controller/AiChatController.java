package org.health.controller;

import org.health.pojo.AiChatRequest;
import org.health.pojo.AiChatResponse;
import org.health.pojo.Result;
import org.health.service.AiChatService;
import org.health.util.UserContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/ai")
public class AiChatController {
    private final AiChatService aiChatService;

    public AiChatController(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    @PostMapping("/chat")
    public Result<AiChatResponse> chat(@RequestBody AiChatRequest request) {
        return Result.success(aiChatService.chat(UserContext.getUserId(), request.getMessage()));
    }

    @PostMapping("/daily-report")
    public Result<AiChatResponse> dailyReport() {
        return Result.success(aiChatService.dailyReport(UserContext.getUserId()));
    }

    @PostMapping("/food-image")
    public Result<AiChatResponse> identifyFood(@RequestPart("image") MultipartFile image) {
        return Result.success(aiChatService.identifyFood(UserContext.getUserId(), image));
    }

    @PostMapping("/workout-image")
    public Result<AiChatResponse> identifyWorkout(@RequestPart("image") MultipartFile image) {
        return Result.success(aiChatService.identifyWorkout(UserContext.getUserId(), image));
    }
}
