---- seckill.lua: 秒杀资格判断（原子性：判库存 + 判重复 + 扣库存 + 记录用户）
--local flashSaleId = ARGV[1]
--local userId = ARGV[2]
--
--local stockKey = 'flash:stock:' .. flashSaleId
--local orderKey = 'flash:order:' .. flashSaleId
--
---- 1. 判断库存是否充足
--if tonumber(redis.call('get', stockKey)) <= 0 then
--    return 1
--end
--
---- 2. 判断用户是否已参与
--if redis.call('sismember', orderKey, userId) == 1 then
--    return 2
--end
--
---- 3. 扣减库存
--redis.call('decr', stockKey)
--
---- 4. 记录用户
--redis.call('sadd', orderKey, userId)
--
---- 5. 返回0表示成功
--return 0

-- seckill.lua: 秒杀资格判断（原子性：判库存 + 判重复 + 扣库存 + 记录用户）
local flashSaleId = ARGV[1]
local userId = ARGV[2]

local stockKey = 'flash:stock:' .. flashSaleId
local orderKey = 'flash:order:' .. flashSaleId

-- 1. 获取库存，key不存在则默认0，避免nil
local stockStr = redis.call('get', stockKey)
local stock = tonumber(stockStr or 0)

if stock <= 0 then
    return 1 -- 库存不足
end

-- 2. 判断用户是否已参与
if redis.call('sismember', orderKey, userId) == 1 then
    return 2 -- 重复抢购
end

-- 3. 扣减库存
redis.call('decr', stockKey)

-- 4. 记录用户
redis.call('sadd', orderKey, userId)

-- 5. 返回0表示成功
return 0
