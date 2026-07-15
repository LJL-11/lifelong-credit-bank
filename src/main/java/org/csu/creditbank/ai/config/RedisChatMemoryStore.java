package org.csu.creditbank.ai.config;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Redis 持久化聊天记忆存储 — 按 userId 隔离，重启不丢。
 * 每个用户最多保留 20 条消息，7 天 TTL 自动过期。
 */
public class RedisChatMemoryStore implements ChatMemoryStore {

    private static final int MAX_MESSAGES = 20;
    private static final long TTL_DAYS = 7;

    private final StringRedisTemplate redis;

    public RedisChatMemoryStore(StringRedisTemplate redis) {
        this.redis = redis;
    }

    private String key(Object memoryId) {
        return "chat:memory:" + memoryId;
    }

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        List<String> jsonList = redis.opsForList().range(key(memoryId), 0, -1);
        if (jsonList == null || jsonList.isEmpty()) {
            return new ArrayList<>();
        }
        return jsonList.stream()
                .map(ChatMessageDeserializer::messageFromJson)
                .toList();
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        String k = key(memoryId);
        // 覆盖写入：先删旧数据，再批量写入
        redis.delete(k);
        for (ChatMessage msg : messages) {
            redis.opsForList().rightPush(k, ChatMessageSerializer.messageToJson(msg));
        }
        // 只保留最近 N 条（从左侧裁剪）
        redis.opsForList().trim(k, -MAX_MESSAGES, -1);
        redis.expire(k, TTL_DAYS, TimeUnit.DAYS);
    }

    @Override
    public void deleteMessages(Object memoryId) {
        redis.delete(key(memoryId));
    }
}
