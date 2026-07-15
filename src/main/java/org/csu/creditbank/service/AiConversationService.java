package org.csu.creditbank.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.csu.creditbank.entity.AiConversation;

import java.util.List;

public interface AiConversationService extends IService<AiConversation> {
    /** 创建新对话，返回新 ID */
    Long create(Long learnerId, String title);
    /** 查询某学员的所有对话（按更新时间倒序） */
    List<AiConversation> listByLearner(Long learnerId);
    /** 删除对话（逻辑删除 + 清除 Redis 记忆） */
    void deleteConversation(Long conversationId, Long learnerId);
    /** 重命名对话 */
    void rename(Long conversationId, Long learnerId, String newTitle);
}
