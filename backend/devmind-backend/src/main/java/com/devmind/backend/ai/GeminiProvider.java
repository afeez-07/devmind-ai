package com.devmind.backend.ai;

import com.devmind.backend.dto.AIRequest;
import com.google.genai.Client;
import com.google.genai.ResponseStream;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Component
@ConditionalOnProperty(
        name = "AI_PROVIDER",
        havingValue = "gemini",
        matchIfMissing = true
)
public class GeminiProvider implements AIProvider {

    private final Client client;

    private static final String MODEL = "gemini-3.7-flash";

    public GeminiProvider(
            @Value("${GEMINI_API_KEY}") String apiKey
    ) {
        this.client = Client.builder()
                .apiKey(apiKey)
                .build();
    }

    @Override
    public String generateResponse(
            List<AIRequest.ChatMessage> messages
    ) {

        List<Content> contents = buildContents(messages);

        GenerateContentResponse response =
                client.models.generateContent(
                        MODEL,
                        contents,
                        null
                );

        return response.text();
    }

    @Override
    public void streamResponse(
            List<AIRequest.ChatMessage> messages,
            Consumer<String> onChunk
    ) {

        List<Content> contents = buildContents(messages);

        ResponseStream<GenerateContentResponse> responseStream =
                client.models.generateContentStream(
                        MODEL,
                        contents,
                        null
                );

        try {

            for (GenerateContentResponse response : responseStream) {

                String text = response.text();

                if (text != null && !text.isEmpty()) {
                    onChunk.accept(text);
                }
            }

        } finally {
            responseStream.close();
        }
    }

    private List<Content> buildContents(
            List<AIRequest.ChatMessage> messages
    ) {

        List<Content> contents = new ArrayList<>();

        for (AIRequest.ChatMessage message : messages) {

            String role = message.getRole();

            if ("assistant".equals(role)) {
                role = "model";
            }

            contents.add(
                    Content.builder()
                            .role(role)
                            .parts(
                                    List.of(
                                            Part.fromText(
                                                    message.getContent()
                                            )
                                    )
                            )
                            .build()
            );
        }

        return contents;
    }

    @Override
    public String getProviderName() {
        return "gemini";
    }
}