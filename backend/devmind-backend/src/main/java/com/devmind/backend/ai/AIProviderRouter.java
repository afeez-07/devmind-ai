////package com.devmind.backend.ai;
////
////import com.devmind.backend.dto.AIRequest;
////import org.springframework.stereotype.Component;
////
////import java.util.List;
////import java.util.function.Consumer;
////
////@Component
////public class AIProviderRouter {
////
////    private final GeminiProvider geminiProvider;
////    private final GroqProvider groqProvider;
////
////    public AIProviderRouter(
////            GeminiProvider geminiProvider,
////            GroqProvider groqProvider
////    ) {
////        this.geminiProvider = geminiProvider;
////        this.groqProvider = groqProvider;
////    }
////
////    public String generateResponse(
////            List<AIRequest.ChatMessage> messages
////    ) {
////
////        try {
////
////            return geminiProvider.generateResponse(messages);
////
////        } catch (Exception e) {
////
////            if (!isFallbackError(e)) {
////                throw e;
////            }
////
////            System.out.println(
////                    "Gemini failed. Switching to Groq fallback."
////            );
////
////            return groqProvider.generateResponse(messages);
////        }
////    }
////
////    public String getProviderName() {
////        return geminiProvider.getProviderName();
////    }
////
////    public void streamResponse(
////            List<AIRequest.ChatMessage> messages,
////            Consumer<String> onChunk
////    ) {
////
////        try {
////
////            geminiProvider.streamResponse(
////                    messages,
////                    onChunk
////            );
////
////        } catch (Exception e) {
////
////            if (!isFallbackError(e)) {
////                throw e;
////            }
////
////            System.out.println(
////                    "Gemini streaming failed. "
////                            + "Switching to Groq fallback."
////            );
////
////            groqProvider.streamResponse(
////                    messages,
////                    onChunk
////            );
////        }
////    }
////
////    private boolean isFallbackError(Exception e) {
////
////        String message = e.getMessage();
////
////        if (message == null) {
////            return false;
////        }
////
////        String error = message.toLowerCase();
////
////        return error.contains("429")
////                || error.contains("resource_exhausted")
////                || error.contains("quota")
////                || error.contains("rate limit")
////                || error.contains("too many requests")
////                || error.contains("timeout")
////                || error.contains("timed out")
////                || error.contains("connection");
////    }
////}
//
//package com.devmind.backend.ai;
//
//import com.devmind.backend.dto.AIRequest;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//import java.util.function.Consumer;
//
//@Component
//public class AIProviderRouter {
//
//    private final GeminiProvider geminiProvider;
//    private final GroqProvider groqProvider;
//
//    public AIProviderRouter(
//            GeminiProvider geminiProvider,
//            GroqProvider groqProvider
//    ) {
//        this.geminiProvider = geminiProvider;
//        this.groqProvider = groqProvider;
//    }
//
//    public ProviderResponse generateResponse(
//            List<AIRequest.ChatMessage> messages
//    ) {
//
//        try {
//
//            String response =
//                    geminiProvider.generateResponse(messages);
//
//            return new ProviderResponse(
//                    response,
//                    geminiProvider.getProviderName()
//            );
//
//        } catch (Exception e) {
//
//            if (!isFallbackError(e)) {
//                throw e;
//            }
//
//            System.out.println(
//                    "Gemini failed. Switching to Groq fallback."
//            );
//
//            String response =
//                    groqProvider.generateResponse(messages);
//
//            return new ProviderResponse(
//                    response,
//                    groqProvider.getProviderName()
//            );
//        }
//    }
//
//    public String getPrimaryProviderName() {
//        return geminiProvider.getProviderName();
//    }
//
//    public void streamResponse(
//            List<AIRequest.ChatMessage> messages,
//            Consumer<String> onChunk
//    ) {
//
//        try {
//
//            geminiProvider.streamResponse(
//                    messages,
//                    onChunk
//            );
//
//        } catch (Exception e) {
//
//            if (!isFallbackError(e)) {
//                throw e;
//            }
//
//            System.out.println(
//                    "Gemini streaming failed. "
//                            + "Switching to Groq fallback."
//            );
//
//            groqProvider.streamResponse(
//                    messages,
//                    onChunk
//            );
//        }
//    }
//
//    private boolean isFallbackError(Exception e) {
//
//        Throwable current = e;
//
//        while (current != null) {
//
//            String message = current.getMessage();
//
//            if (message != null) {
//
//                String error =
//                        message.toLowerCase();
//
//                if (error.contains("429")
//                        || error.contains("resource_exhausted")
//                        || error.contains("quota")
//                        || error.contains("rate limit")
//                        || error.contains("too many requests")
//                        || error.contains("timeout")
//                        || error.contains("timed out")
//                        || error.contains("connection")) {
//
//                    return true;
//                }
//            }
//
//            current = current.getCause();
//        }
//
//        return false;
//    }
//
//    public record ProviderResponse(
//            String response,
//            String provider
//    ) {
//    }
//}

package com.devmind.backend.ai;

import com.devmind.backend.dto.AIRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@Component
public class AIProviderRouter {

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

            String response = geminiProvider.generateResponse(messages);

            return new ProviderResponse(
                    response,
                    geminiProvider.getProviderName()
            );

        } catch (Exception e) {

            if (!isFallbackError(e)) {
                throw e;
            }

            System.out.println(
                    "Gemini failed. Switching to Groq fallback."
            );

            String response = groqProvider.generateResponse(messages);

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

            geminiProvider.streamResponse(
                    messages,
                    chunk -> {

                        hasStartedStreaming.set(true);

                        onChunk.accept(chunk);
                    }
            );

        } catch (Exception e) {

            /*
             * If Gemini already sent content to the user,
             * do NOT restart the response using Groq.
             */
            if (hasStartedStreaming.get()) {
                throw e;
            }

            if (!isFallbackError(e)) {
                throw e;
            }

            System.out.println(
                    "Gemini streaming failed. Switching to Groq fallback."
            );

            groqProvider.streamResponse(
                    messages,
                    onChunk
            );
        }
    }

    private boolean isFallbackError(Exception e) {

        Throwable current = e;

        while (current != null) {

            String message = current.getMessage();

            if (message != null) {

                String error = message.toLowerCase();

                if (
                        error.contains("429")
                                || error.contains("resource_exhausted")
                                || error.contains("quota")
                                || error.contains("rate limit")
                                || error.contains("too many requests")
                                || error.contains("timeout")
                                || error.contains("timed out")
                                || error.contains("connection")
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

    public record ProviderResponse(
            String response,
            String provider
    ) {
    }
}