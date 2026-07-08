package org.csu.creditbank.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.csu.creditbank.common.BusinessException;
import org.csu.creditbank.entity.ForumPostLike;
import org.csu.creditbank.mapper.ForumPostLikeMapper;
import org.csu.creditbank.service.ForumPostLikeService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ForumPostLikeServiceImpl extends ServiceImpl<ForumPostLikeMapper, ForumPostLike>
        implements ForumPostLikeService {

    private static final String LIKED_ZSET_KEY = "forum:liked:";
    private static final String LOCK_KEY = "lock:forum:like:";

    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;

    public ForumPostLikeServiceImpl(StringRedisTemplate stringRedisTemplate,
                                     RedissonClient redissonClient) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.redissonClient = redissonClient;
    }

    // ==================== 点赞 ====================

    @Override
    public void like(Long postId, Long userId) {
        String lockKey = LOCK_KEY + postId + ":" + userId;
        RLock lock = redissonClient.getLock(lockKey);
        if (!lock.tryLock()) {
            throw new BusinessException("操作太频繁，请稍后再试");
        }
        try {
            _like(postId, userId);
        } finally {
            lock.unlock();
        }
    }

    private void _like(Long postId, Long userId) {
        // 1. Redis 快速判重
        String zsetKey = LIKED_ZSET_KEY + postId;
        Double score = stringRedisTemplate.opsForZSet().score(zsetKey, userId.toString());
        if (score != null) {
            throw new BusinessException("已经点过赞了");
        }

        // 2. 数据库兜底判重
        boolean exists = lambdaQuery()
                .eq(ForumPostLike::getPostId, postId)
                .eq(ForumPostLike::getLearnerId, userId)
                .exists();
        if (exists) {
            // DB 有但 Redis 没有 → 补偿写入 Redis
            stringRedisTemplate.opsForZSet().add(zsetKey, userId.toString(), System.currentTimeMillis());
            throw new BusinessException("已经点过赞了");
        }

        // 3. 写入数据库
        ForumPostLike like = new ForumPostLike();
        like.setPostId(postId);
        like.setLearnerId(userId);
        save(like);

        // 4. 写入 Redis ZSet
        stringRedisTemplate.opsForZSet().add(zsetKey, userId.toString(), System.currentTimeMillis());
    }

    // ==================== 取消点赞 ====================

    @Override
    public void unlike(Long postId, Long userId) {
        String lockKey = LOCK_KEY + postId + ":" + userId;
        RLock lock = redissonClient.getLock(lockKey);
        if (!lock.tryLock()) {
            throw new BusinessException("操作太频繁，请稍后再试");
        }
        try {
            _unlike(postId, userId);
        } finally {
            lock.unlock();
        }
    }

    private void _unlike(Long postId, Long userId) {
        String zsetKey = LIKED_ZSET_KEY + postId;

        // 1. Redis 快速判断
        Double score = stringRedisTemplate.opsForZSet().score(zsetKey, userId.toString());
        if (score == null) {
            throw new BusinessException("还没有点赞");
        }

        // 2. 删除数据库记录
        lambdaUpdate()
                .eq(ForumPostLike::getPostId, postId)
                .eq(ForumPostLike::getLearnerId, userId)
                .remove();

        // 3. 从 Redis ZSet 移除
        stringRedisTemplate.opsForZSet().remove(zsetKey, userId.toString());
    }

    // ==================== 批量查询 ====================

    @Override
    public Map<Long, Integer> getLikeCounts(Collection<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) return Collections.emptyMap();
        Map<Long, Integer> result = new HashMap<>();
        for (Long postId : postIds) {
            String zsetKey = LIKED_ZSET_KEY + postId;
            Long count = stringRedisTemplate.opsForZSet().zCard(zsetKey);
            if (count != null && count > 0) {
                result.put(postId, count.intValue());
            } else {
                // Redis 冷启动 → 查数据库并回填
                Long longCount = lambdaQuery().eq(ForumPostLike::getPostId, postId).count();
                int dbCount = Optional.ofNullable(longCount).orElse(0L).intValue();
                result.put(postId, dbCount);
                if (dbCount > 0) {
                    // 从 DB 回填到 Redis
                    List<ForumPostLike> likes = lambdaQuery()
                            .eq(ForumPostLike::getPostId, postId).list();
                    for (ForumPostLike like : likes) {
                        stringRedisTemplate.opsForZSet().add(zsetKey,
                                like.getLearnerId().toString(),
                                like.getCreatedAt() != null
                                        ? like.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                                        : System.currentTimeMillis());
                    }
                }
            }
        }
        return result;
    }

    @Override
    public Set<Long> getLikedPostIds(Collection<Long> postIds, Long userId) {
        if (postIds == null || postIds.isEmpty() || userId == null) return Collections.emptySet();
        Set<Long> liked = new HashSet<>();
        for (Long postId : postIds) {
            String zsetKey = LIKED_ZSET_KEY + postId;
            Double score = stringRedisTemplate.opsForZSet().score(zsetKey, userId.toString());
            if (score != null) {
                liked.add(postId);
            } else {
                // 冷启动：Redis 无数据，查 DB
                boolean exists = lambdaQuery()
                        .eq(ForumPostLike::getPostId, postId)
                        .eq(ForumPostLike::getLearnerId, userId)
                        .exists();
                if (exists) {
                    liked.add(postId);
                    // 回填 Redis
                    stringRedisTemplate.opsForZSet().add(zsetKey, userId.toString(), System.currentTimeMillis());
                }
            }
        }
        return liked;
    }
}
