package com.devmind.backend.ai;

import com.devmind.backend.dto.AIRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@Component
public class AIProviderRouter {

    private static final Logger log =
            LoggerFactory.getLogger(AIProviderRouter.class);

    private final GeminiProvider geminiProvider;
    private final GroqProvider groqProvider;

    public AIProviderRouter(
            GeminiProvider geminiProvider,
            GroqProvider groqProvider
    ) {
        this.geminiProvider = geminiProvider;
        this.groqProvider = groqProvider;
    }

    public ProviderResponse generateResponse(
            List<AIRequest.ChatMessage> messages
    ) {

        try {

            log.info("Trying Gemini provider...");

            String response =
                    geminiProvider.generateResponse(messages);

            log.info("Gemini response received successfully.");

            return new ProviderResponse(
                    response,
                    geminiProvider.getProviderName()
            );

        } catch (Exception e) {

            log.error(
                    "GEMINI GENERATE FAILED - exception details:",
                    e
            );

            logExceptionChain(e);

            if (!isFallbackError(e)) {
                throw e;
            }

            log.warn(
                    "Gemini failed. Switching to Groq fallback."
            );

            String response =
                    groqProvider.generateResponse(messages);

            log.info(
                    "Groq fallback response received successfully."
            );

            return new ProviderResponse(
                    response,
                    groqProvider.getProviderName()
            );
        }
    }

    public String getPrimaryProviderName() {
        return geminiProvider.getProviderName();
    }

    public void streamResponse(
            List<AIRequest.ChatMessage> messages,
            Consumer<String> onChunk
    ) {

        AtomicBoolean hasStartedStreaming =
                new AtomicBoolean(false);

        try {

            log.info("Trying Gemini streaming provider...");

            geminiProvider.streamResponse(
                    messages,
                    chunk -> {

                        hasStartedStreaming.set(true);

                        onChunk.accept(chunk);
                    }
            );

            log.info(
                    "Gemini streaming completed successfully."
            );

        } catch (Exception e) {

            log.error(
                    "GEMINI STREAMING FAILED - exception details:",
                    e
            );

            logExceptionChain(e);

            /*
             * If Gemini already sent content to the user,
             * do NOT restart the response using Groq.
             */
            if (hasStartedStreaming.get()) {

                log.warn(
                        "Gemini already started streaming. " +
                                "Not switching to Groq."
                );

                throw e;
            }

            if (!isFallbackError(e)) {

                log.warn(
                        "Gemini error is not marked as a fallback error."
                );

                throw e;
            }

            log.warn(
                    "Gemini streaming failed. " +
                            "Switching to Groq fallback."
            );

            groqProvider.streamResponse(
                    messages,
                    onChunk
            );

            log.info(
                    "Groq streaming fallback completed."
            );
        }
    }

    private boolean isFallbackError(Exception e) {

        Throwable current = e;

        while (current != null) {

            String message = current.getMessage();

            if (message != null) {

                String error =
                        message.toLowerCase();

                if (
                        error.contains("429")
                                || error.contains("503")
                                || error.contains("500")
                                || error.contains("resource_exhausted")
                                || error.contains("quota")
                                || error.contains("rate limit")
                                || error.contains("too many requests")
                                || error.contains("timeout")
                                || error.contains("timed out")
                                || error.contains("connection")
                                || error.contains("unavailable")
                                || error.contains("service unavailable")
                                || error.contains("api_key_invalid")
                                || error.contains("api key not valid")
                ) {
                    return true;
                }
            }

            current = current.getCause();
        }

        return false;
    }

    private void logExceptionChain(Throwable e) {

        Throwable current = e;

        int level = 0;

        while (current != null) {

            log.error(
                    "Gemini exception [{}] - {}: {}",
                    level,
                    current.getClass().getName(),
                    current.getMessage()
            );

            current = current.getCause();

            level++;
        }
    }

    public record ProviderResponse(
            String response,
            String provider
    ) {
    }
}