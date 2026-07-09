package org.csu.creditbank.ai.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.csu.creditbank.ai.assistant.LearningAssistant;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@RequestMapping("/api/student/ai")
public class LearningAssistantController {

    private final LearningAssistant assistant;

    public LearningAssistantController(LearningAssistant assistant) {
        this.assistant = assistant;
    }

    @PostMapping(value = "/chat", produces = "text/stream;charset=utf-8")
    public Flux<String> chat(@RequestBody Map<String, String> body,
                              HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String userMessage = body.getOrDefault("message", "");

        // 把 userId 注入消息：LLM 无需询问即可调用需要 userId 的工具
        String fullMessage = userMessage
                + "\n\n[系统提示：当前用户ID是 " + userId + "，查询或操作我的数据时直接使用这个ID]";

        return assistant.chat(userId, fullMessage);
    }
}
