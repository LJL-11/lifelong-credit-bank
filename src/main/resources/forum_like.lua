-- forum_like.lua: 原子性点赞/取消点赞（仅操作 Redis ZSet）
-- 返回值: 0=点赞成功, 1=取消点赞成功, 2=已点赞（重复点赞）
-- KEYS[1] = forum:liked:{postId}
-- ARGV[1] = userId

local zsetKey = KEYS[1]
local userId = ARGV[1]

-- 判断是否已点赞
local score = redis.call('ZSCORE', zsetKey, userId)

if score then
    -- 已点赞 → 取消点赞
    redis.call('ZREM', zsetKey, userId)
    return 1
else
    -- 未点赞 → 点赞
    redis.call('ZADD', zsetKey, redis.call('TIME')[1], userId)
    return 0
end
