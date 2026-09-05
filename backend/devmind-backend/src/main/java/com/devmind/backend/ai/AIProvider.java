package com.devmind.backend.ai;

import com.devmind.backend.dto.AIRequest;

import java.util.List;
import java.util.function.Consumer;

public interface AIProvider {

    String generateResponse(List<AIRequest.ChatMessage> messages);

    void streamResponse(
            List<AIRequest.ChatMessage> messages,
            Consumer<String> onChunk
    );

    String getProviderName();

}