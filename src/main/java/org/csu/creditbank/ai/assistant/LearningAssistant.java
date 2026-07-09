package org.csu.creditbank.ai.assistant;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import reactor.core.publisher.Flux;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;

@AiService(
        wiringMode = EXPLICIT,
        streamingChatModel = "openAiStreamingChatModel",
        chatMemoryProvider = "chatMemoryProvider",
        tools = "learningTools",
        contentRetriever = "contentRetriever"
)
public interface LearningAssistant {

    @SystemMessage(fromResource = "learning-assistant-prompt.txt")
    Flux<String> chat(@MemoryId Long userId, @UserMessage String userMessage);
}
