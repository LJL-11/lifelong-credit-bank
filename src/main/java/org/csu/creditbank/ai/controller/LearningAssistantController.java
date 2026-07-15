package org.csu.creditbank.ai.controller;

import jakarta.servlet.http.HttpServletRequest;
import dev.langchain4j.data.message.*;
import org.csu.creditbank.ai.assistant.LearningAssistant;
import org.csu.creditbank.common.ApiResult;
import org.csu.creditbank.entity.AiConversation;
import org.csu.creditbank.service.AiConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.*;

@RestController
@RequestMapping("/api/student/ai")
public class LearningAssistantController {

    @Autowired(required = false)
    private LearningAssistant assistant;

    private final AiConversationService conversationService;
    private final StringRedisTemplate redis;

    public LearningAssistantController(AiConversationService conversationService,
                                        StringRedisTemplate redis) {
        this.conversationService = conversationService;
        this.redis = redis;
    }

    /** 对话列表 */
    @GetMapping("/conversations")
    public ApiResult<List<AiConversation>> listConversations(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResult.ok(conversationService.listByLearner(userId));
    }

    /** 新建对话 */
    @PostMapping("/conversations")
    public ApiResult<AiConversation> createConversation(@RequestBody Map<String, String> body,
                                                         HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String title = body.getOrDefault("title", "新对话");
        Long id = conversationService.create(userId, title);
        AiConversation conv = conversationService.getById(id);
        return ApiResult.ok(conv);
    }

    /** 删除对话 */
    @DeleteMapping("/conversations/{id}")
    public ApiResult<String> deleteConversation(@PathVariable Long id,
                                                 HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        conversationService.deleteConversation(id, userId);
        return ApiResult.ok("ok");
    }

    /** 重命名对话 */
    @PutMapping("/conversations/{id}")
    public ApiResult<String> renameConversation(@PathVariable Long id,
                                                 @RequestBody Map<String, String> body,
                                                 HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        conversationService.rename(id, userId, body.getOrDefault("title", "新对话"));
        return ApiResult.ok("ok");
    }

    /** 查询某个对话的消息历史（从 Redis 中读取） */
    @GetMapping("/conversations/{id}/messages")
    public ApiResult<List<Map<String, Object>>> getMessages(@PathVariable Long id,
                                                              HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        AiConversation conv = conversationService.getById(id);
        if (conv == null || !conv.getLearnerId().equals(userId)) {
            return ApiResult.fail("对话不存在");
        }
        String key = "chat:memory:" + id;
        List<String> jsonList = redis.opsForList().range(key, 0, -1);
        if (jsonList == null || jsonList.isEmpty()) {
            return ApiResult.ok(Collections.emptyList());
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (String json : jsonList) {
            try {
                ChatMessage msg = ChatMessageDeserializer.messageFromJson(json);
                if (msg instanceof UserMessage) {
                    String content = ((UserMessage) msg).singleText();
                    if (content.contains("[系统提示：")) {
                        content = content.substring(0, content.indexOf("[系统提示：")).trim();
                    }
                    if (!content.isEmpty()) {
                        Map<String, Object> item = new LinkedHashMap<>();
                        item.put("isUser", true);
                        item.put("content", content);
                        result.add(item);
                    }
                } else if (msg instanceof AiMessage) {
                    String content = ((AiMessage) msg).text();
                    if (content != null && !content.isEmpty()) {
                        Map<String, Object> item = new LinkedHashMap<>();
                        item.put("isUser", false);
                        item.put("content", content);
                        result.add(item);
                    }
                }
            } catch (Exception ignored) {}
        }
        return ApiResult.ok(result);
    }

    /** 发送消息 — 使用 conversationId 隔离记忆 */
    @PostMapping(value = "/chat", produces = "text/stream;charset=utf-8")
    public Flux<String> chat(@RequestBody Map<String, String> body,
                              HttpServletRequest request) {
        if (assistant == null) {
            return Flux.just("AI 助手暂未配置，请设置环境变量 deepseek 为有效的 DeepSeek API Key 后重启服务。");
        }
        Long userId = (Long) request.getAttribute("userId");
        String userMessage = body.getOrDefault("message", "");
        String convIdStr = body.getOrDefault("conversationId", "0");
        Long conversationId = Long.valueOf(convIdStr);

        if (conversationId == 0) {
            String title = userMessage.length() > 20 ? userMessage.substring(0, 20) : userMessage;
            conversationId = conversationService.create(userId, title);
        }

        String fullMessage = userMessage
                + "\n\n[系统提示：当前用户ID是 " + userId + "，查询或操作我的数据时直接使用这个ID]";

        final Long cid = conversationId;
        return assistant.chat(cid, fullMessage)
                .doOnComplete(() -> {
                    AiConversation conv = conversationService.getById(cid);
                    if (conv != null) {
                        conversationService.updateById(conv);
                    }
                });
    }
}
