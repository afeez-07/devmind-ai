package com.devmind.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import com.devmind.backend.dto.AIRequest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OllamaService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OllamaService() {
        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:11434")
                .build();
    }

    public String generateResponse(List<AIRequest.ChatMessage> messages) {

        List<Map<String, String>> ollamaMessages = messages.stream()
                .map(message -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("role", message.getRole());
                    map.put("content", message.getContent());
                    return map;
                })
                .toList();

        Map<String, Object> requestBody = Map.of(
                "model", "qwen2.5-coder:1.5b",
                "messages", ollamaMessages,
                "stream", false,
                "keep_alive", 0
        );

        Map response = restClient.post()
                .uri("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        if (response == null || response.get("message") == null) {
            throw new RuntimeException("No response received from Ollama");
        }

        Map messageResponse = (Map) response.get("message");

        return (String) messageResponse.get("content");
    }

    public void streamResponse(
            List<AIRequest.ChatMessage> messages,
            Consumer<String> onChunk
    ) {

        List<Map<String, String>> ollamaMessages = messages.stream()
                .map(message -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("role", message.getRole());
                    map.put("content", message.getContent());
                    return map;
                })
                .toList();

        Map<String, Object> requestBody = Map.of(
                "model", "qwen2.5-coder:1.5b",
                "messages", ollamaMessages,
                "stream", true,
                "keep_alive", 0
        );

        restClient.post()
                .uri("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .exchange((request, response) -> {

                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(
                                    response.getBody(),
                                    StandardCharsets.UTF_8
                            ))) {

                        String line;

                        while ((line = reader.readLine()) != null) {

                            if (line.isBlank()) {
                                continue;
                            }

                            JsonNode json = objectMapper.readTree(line);

                            JsonNode messageNode = json.get("message");

                            if (messageNode != null) {

                                JsonNode contentNode =
                                        messageNode.get("content");

                                if (contentNode != null) {
                                    onChunk.accept(contentNode.asText());
                                }
                            }

                            if (json.path("done").asBoolean(false)) {
                                break;
                            }
                        }
                    }

                    return null;
                });
    }
}