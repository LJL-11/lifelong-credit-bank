package org.csu.creditbank.ai.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.csu.creditbank.ai.assistant.LearningAssistant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@RequestMapping("/api/student/ai")
public class LearningAssistantController {

    @Autowired(required = false)
    private LearningAssistant assistant;

    @PostMapping(value = "/chat", produces = "text/stream;charset=utf-8")
    public Flux<String> chat(@RequestBody Map<String, String> body,
                              HttpServletRequest request) {
        if (assistant == null) {
            return Flux.just("AI 助手暂未配置，请设置环境变量 deepseek 为有效的 DeepSeek API Key 后重启服务。");
        }
        Long userId = (Long) request.getAttribute("userId");
        String userMessage = body.getOrDefault("message", "");

        String fullMessage = userMessage
                + "\n\n[系统提示：当前用户ID是 " + userId + "，查询或操作我的数据时直接使用这个ID]";

        return assistant.chat(userId, fullMessage);
    }
}
