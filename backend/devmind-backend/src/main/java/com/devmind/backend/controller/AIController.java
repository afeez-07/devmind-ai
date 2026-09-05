package com.devmind.backend.controller;

import java.util.Map;
import com.devmind.backend.dto.AIRequest;
import com.devmind.backend.dto.AIResponse;
import com.devmind.backend.service.AIService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final AIService aiService;

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/chat")
    public ResponseEntity<AIResponse> chat(
            @RequestBody AIRequest request
    ) {

        if (request.getMessages() == null ||
                request.getMessages().isEmpty()) {

            return ResponseEntity.badRequest().build();
        }

        AIResponse response = aiService.chat(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/provider")
    public ResponseEntity<Map<String, String>> getProvider() {

        return ResponseEntity.ok(
                Map.of(
                        "provider",
                        aiService.getProviderName()
                )
        );
    }

    @PostMapping(
            value = "/chat/stream",
            produces = "text/event-stream"
    )
    public SseEmitter streamChat(
            @RequestBody AIRequest request
    ) {

        SseEmitter emitter = new SseEmitter(120000L);

        new Thread(() -> {
            aiService.streamChat(request, emitter);
        }).start();

        return emitter;
    }
}