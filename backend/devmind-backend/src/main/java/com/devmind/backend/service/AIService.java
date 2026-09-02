package com.devmind.backend.service;

import com.devmind.backend.dto.AIRequest;
import com.devmind.backend.dto.AIResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class AIService {

    private final OllamaService ollamaService;

    public AIService(OllamaService ollamaService) {
        this.ollamaService = ollamaService;
    }

    public AIResponse chat(AIRequest request) {

        if (request.getMessages() == null || request.getMessages().isEmpty()) {
            throw new IllegalArgumentException("Messages cannot be empty");
        }

        String response = ollamaService.generateResponse(
                request.getMessages()
        );

        return new AIResponse(
                response,
                "ollama"
        );
    }

    public void streamChat(
            AIRequest request,
            SseEmitter emitter
    ) {

        if (request.getMessages() == null ||
                request.getMessages().isEmpty()) {

            emitter.completeWithError(
                    new IllegalArgumentException("Messages cannot be empty")
            );

            return;
        }

        try {

            ollamaService.streamResponse(
                    request.getMessages(),
                    chunk -> {

                        try {

                            emitter.send(
                                    SseEmitter.event()
                                            .data(chunk)
                            );

                        } catch (Exception e) {

                            emitter.completeWithError(e);
                        }
                    }
            );

            emitter.complete();

        } catch (Exception e) {

            emitter.completeWithError(e);
        }
    }
}