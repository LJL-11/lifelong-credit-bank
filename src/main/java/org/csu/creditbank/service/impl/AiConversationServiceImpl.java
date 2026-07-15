package org.csu.creditbank.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.csu.creditbank.common.BusinessException;
import org.csu.creditbank.entity.AiConversation;
import org.csu.creditbank.mapper.AiConversationMapper;
import org.csu.creditbank.service.AiConversationService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiConversationServiceImpl extends ServiceImpl<AiConversationMapper, AiConversation>
        implements AiConversationService {

    private final StringRedisTemplate redis;

    public AiConversationServiceImpl(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public Long create(Long learnerId, String title) {
        AiConversation conv = new AiConversation();
        conv.setLearnerId(learnerId);
        conv.setTitle(title != null && !title.isBlank() ? title : "新对话");
        save(conv);
        return conv.getId();
    }

    @Override
    public List<AiConversation> listByLearner(Long learnerId) {
        return lambdaQuery()
                .eq(AiConversation::getLearnerId, learnerId)
                .orderByDesc(AiConversation::getUpdatedAt)
                .list();
    }

    @Override
    public void deleteConversation(Long conversationId, Long learnerId) {
        AiConversation conv = getById(conversationId);
        if (conv == null || !conv.getLearnerId().equals(learnerId)) {
            throw new BusinessException("对话不存在");
        }
        removeById(conversationId);
        // 清除 Redis 记忆
        redis.delete("chat:memory:" + conversationId);
    }

    @Override
    public void rename(Long conversationId, Long learnerId, String newTitle) {
        AiConversation conv = getById(conversationId);
        if (conv == null || !conv.getLearnerId().equals(learnerId)) {
            throw new BusinessException("对话不存在");
        }
        conv.setTitle(newTitle);
        updateById(conv);
    }
}
