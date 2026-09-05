package com.devmind.backend.ai;

import com.devmind.backend.dto.AIRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Component
public class GroqProvider implements AIProvider {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    private static final String MODEL = "openai/gpt-oss-120b";

    public GroqProvider(
            @Value("${GROQ_API_KEY:}") String apiKey
    ) {
        this.apiKey = apiKey;

        this.restClient = RestClient.builder()
                .baseUrl("https://api.groq.com/openai/v1")
                .build();

        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String generateResponse(
            List<AIRequest.ChatMessage> messages
    ) {

        List<Map<String, String>> groqMessages =
                messages.stream()
                        .map(message -> {
                            Map<String, String> map = new HashMap<>();
                            map.put("role", message.getRole());
                            map.put("content", message.getContent());
                            return map;
                        })
                        .toList();

        Map<String, Object> requestBody = Map.of(
                "model", MODEL,
                "messages", groqMessages,
                "stream", false
        );

        Map response = restClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        if (response == null || response.get("choices") == null) {
            throw new RuntimeException(
                    "No response received from Groq"
            );
        }

        List choices = (List) response.get("choices");

        if (choices.isEmpty()) {
            throw new RuntimeException(
                    "Groq returned no choices"
            );
        }

        Map choice = (Map) choices.get(0);
        Map message = (Map) choice.get("message");

        if (message == null || message.get("content") == null) {
            throw new RuntimeException(
                    "Groq returned an empty response"
            );
        }

        return (String) message.get("content");
    }

    @Override
    public void streamResponse(
            List<AIRequest.ChatMessage> messages,
            Consumer<String> onChunk
    ) {

        List<Map<String, String>> groqMessages =
                messages.stream()
                        .map(message -> {
                            Map<String, String> map = new HashMap<>();
                            map.put("role", message.getRole());
                            map.put("content", message.getContent());
                            return map;
                        })
                        .toList();

        Map<String, Object> requestBody = Map.of(
                "model", MODEL,
                "messages", groqMessages,
                "stream", true
        );

        restClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .exchange((request, response) -> {

                    try (BufferedReader reader =
                                 new BufferedReader(
                                         new InputStreamReader(
                                                 response.getBody(),
                                                 StandardCharsets.UTF_8
                                         ))) {

                        String line;

                        while ((line = reader.readLine()) != null) {

                            if (line.isBlank()) {
                                continue;
                            }

                            if (!line.startsWith("data:")) {
                                continue;
                            }

                            String data =
                                    line.substring(5).trim();

                            if ("[DONE]".equals(data)) {
                                break;
                            }

                            JsonNode json =
                                    objectMapper.readTree(data);

                            JsonNode choices =
                                    json.get("choices");

                            if (choices == null ||
                                    choices.isEmpty()) {
                                continue;
                            }

                            JsonNode delta =
                                    choices.get(0).get("delta");

                            if (delta == null) {
                                continue;
                            }

                            JsonNode content =
                                    delta.get("content");

                            if (content != null &&
                                    !content.isNull()) {

                                String text =
                                        content.asText();

                                if (!text.isEmpty()) {
                                    onChunk.accept(text);
                                }
                            }
                        }

                    } catch (Exception e) {
                        throw new RuntimeException(
                                "Groq streaming failed",
                                e
                        );
                    }

                    return null;
                });
    }

    @Override
    public String getProviderName() {
        return "groq";
    }
}