package org.csu.creditbank.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.csu.creditbank.entity.ForumPostLike;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface ForumPostLikeService extends IService<ForumPostLike> {

    /** 点赞（Redisson 分布式锁防并发） */
    void like(Long postId, Long userId);

    /** 取消点赞（Redisson 分布式锁防并发） */
    void unlike(Long postId, Long userId);

    /** 批量获取帖子点赞数 */
    Map<Long, Integer> getLikeCounts(Collection<Long> postIds);

    /** 批量获取当前用户已点赞的帖子ID集合 */
    Set<Long> getLikedPostIds(Collection<Long> postIds, Long userId);
}
