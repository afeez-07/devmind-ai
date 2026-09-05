////package com.devmind.backend.service;
////
////import java.util.Map;
////import com.devmind.backend.ai.AIProvider;
////import com.devmind.backend.dto.AIRequest;
////import com.devmind.backend.dto.AIResponse;
////import org.springframework.stereotype.Service;
////import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
////
////@Service
////public class AIService {
////
////    private final AIProvider aiProvider;
////
////    public AIService(AIProvider aiProvider) {
////        this.aiProvider = aiProvider;
////    }
////
////    public String getProviderName() {
////        return aiProvider.getProviderName();
////    }
////
////    public AIResponse chat(AIRequest request) {
////
////        if (request.getMessages() == null ||
////                request.getMessages().isEmpty()) {
////
////            throw new IllegalArgumentException("Messages cannot be empty");
////        }
////
////        String response = aiProvider.generateResponse(
////                request.getMessages()
////        );
////
////        return new AIResponse(
////                response,
////                aiProvider.getProviderName()
////        );
////    }
////
////    public void streamChat(
////            AIRequest request,
////            SseEmitter emitter
////    ) {
////
////        if (request.getMessages() == null ||
////                request.getMessages().isEmpty()) {
////
////            emitter.completeWithError(
////                    new IllegalArgumentException("Messages cannot be empty")
////            );
////
////            return;
////        }
////
////        try {
////
////            aiProvider.streamResponse(
////                    request.getMessages(),
////                    chunk -> {
////
////                        try {
////
////                            // Send the chunk with a prefix so that
////                            // leading spaces from the AI response
////                            // are preserved during SSE transmission.
////
////                            emitter.send(
////                                    SseEmitter.event()
////                                            .data(Map.of("content", chunk))
////                            );
////
////                        } catch (Exception e) {
////                            emitter.completeWithError(e);
////                        }
////                    }
////            );
////
////            emitter.complete();
////
////        } catch (Exception e) {
////            emitter.completeWithError(e);
////        }
////    }
////}
//
//package com.devmind.backend.service;
//
//import java.util.Map;
//
//import com.devmind.backend.ai.AIProviderRouter;
//import com.devmind.backend.dto.AIRequest;
//import com.devmind.backend.dto.AIResponse;
//
//import org.springframework.stereotype.Service;
//import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
//
//@Service
//public class AIService {
//
//    private final AIProviderRouter aiProviderRouter;
//
//    public AIService(AIProviderRouter aiProviderRouter) {
//        this.aiProviderRouter = aiProviderRouter;
//    }
//
//    public String getProviderName() {
//        return aiProviderRouter.getProviderName();
//    }
//
//    public AIResponse chat(AIRequest request) {
//
//        if (request.getMessages() == null ||
//                request.getMessages().isEmpty()) {
//
//            throw new IllegalArgumentException(
//                    "Messages cannot be empty"
//            );
//        }
//
//        String response =
//                aiProviderRouter.generateResponse(
//                        request.getMessages()
//                );
//
//        return new AIResponse(
//                response,
//                getActualProvider(response)
//        );
//    }
//
//    public void streamChat(
//            AIRequest request,
//            SseEmitter emitter
//    ) {
//
//        if (request.getMessages() == null ||
//                request.getMessages().isEmpty()) {
//
//            emitter.completeWithError(
//                    new IllegalArgumentException(
//                            "Messages cannot be empty"
//                    )
//            );
//
//            return;
//        }
//
//        try {
//
//            aiProviderRouter.streamResponse(
//                    request.getMessages(),
//                    chunk -> {
//
//                        try {
//
//                            emitter.send(
//                                    SseEmitter.event()
//                                            .data(
//                                                    Map.of(
//                                                            "content",
//                                                            chunk
//                                                    )
//                                            )
//                            );
//
//                        } catch (Exception e) {
//                            emitter.completeWithError(e);
//                        }
//                    }
//            );
//
//            emitter.complete();
//
//        } catch (Exception e) {
//
//            emitter.completeWithError(e);
//        }
//    }
//
//    private String getActualProvider(String response) {
//
//        // Temporary implementation.
//        // Provider tracking will be improved in the next step.
//
//        return aiProviderRouter.getProviderName();
//    }
//}

package com.devmind.backend.service;

import java.util.Map;

import com.devmind.backend.ai.AIProviderRouter;
import com.devmind.backend.dto.AIRequest;
import com.devmind.backend.dto.AIResponse;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class AIService {

    private final AIProviderRouter aiProviderRouter;

    public AIService(AIProviderRouter aiProviderRouter) {
        this.aiProviderRouter = aiProviderRouter;
    }

    public String getProviderName() {

        return aiProviderRouter.getPrimaryProviderName();
    }

    public AIResponse chat(AIRequest request) {

        if (request.getMessages() == null ||
                request.getMessages().isEmpty()) {

            throw new IllegalArgumentException(
                    "Messages cannot be empty"
            );
        }

        AIProviderRouter.ProviderResponse result =
                aiProviderRouter.generateResponse(
                        request.getMessages()
                );

        return new AIResponse(
                result.response(),
                result.provider()
        );
    }

    public void streamChat(
            AIRequest request,
            SseEmitter emitter
    ) {

        if (request.getMessages() == null ||
                request.getMessages().isEmpty()) {

            emitter.completeWithError(
                    new IllegalArgumentException(
                            "Messages cannot be empty"
                    )
            );

            return;
        }

        try {

            aiProviderRouter.streamResponse(
                    request.getMessages(),
                    chunk -> {

                        try {

                            emitter.send(
                                    SseEmitter.event()
                                            .data(
                                                    Map.of(
                                                            "content",
                                                            chunk
                                                    )
                                            )
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