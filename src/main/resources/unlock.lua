-- unlock.lua: 释放分布式锁（防止误删其他线程的锁）
if (redis.call('get', KEYS[1]) == ARGV[1]) then
    return redis.call('del', KEYS[1])
end
return 0
