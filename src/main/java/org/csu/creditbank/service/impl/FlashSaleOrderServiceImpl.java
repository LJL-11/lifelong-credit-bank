package org.csu.creditbank.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.csu.creditbank.common.BusinessException;
import org.csu.creditbank.config.MqConst;
import org.csu.creditbank.entity.*;
import org.csu.creditbank.mapper.FlashSaleRecordMapper;
import org.csu.creditbank.service.*;
import org.csu.creditbank.util.RedisIdWorker;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.UUID;

@Slf4j
@Service
public class FlashSaleOrderServiceImpl extends ServiceImpl<FlashSaleRecordMapper, FlashSaleRecord>
        implements FlashSaleOrderService {

    private final FlashSaleService flashSaleService;
    private final CreditAccountService accountService;
    private final CreditOrderService orderService;
    private final CreditProductService productService;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedisIdWorker redisIdWorker;
    private final RedissonClient redissonClient;
    private final RabbitTemplate rabbitTemplate;

    public FlashSaleOrderServiceImpl(FlashSaleService flashSaleService,
                                      CreditAccountService accountService,
                                      CreditOrderService orderService,
                                      CreditProductService productService,
                                      StringRedisTemplate stringRedisTemplate,
                                      RedisIdWorker redisIdWorker,
                                      RedissonClient redissonClient,
                                      RabbitTemplate rabbitTemplate) {
        this.flashSaleService = flashSaleService;
        this.accountService = accountService;
        this.orderService = orderService;
        this.productService = productService;
        this.stringRedisTemplate = stringRedisTemplate;
        this.redisIdWorker = redisIdWorker;
        this.redissonClient = redissonClient;
        this.rabbitTemplate = rabbitTemplate;
    }

    // Lua 脚本：原子判库存 + 判重复 + 扣库存 + 记录用户
    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;
    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    @Override
    public long seckill(Long flashSaleId, Long learnerId) {
        // 1. 执行 Lua 脚本（原子：判库存 + 判重复 + 扣库存 + 记录用户）
        Long result = stringRedisTemplate.execute(SECKILL_SCRIPT,
                Collections.emptyList(),
                flashSaleId.toString(), learnerId.toString());
        int r = result != null ? result.intValue() : -1;
        if (r != 0) {
            throw new BusinessException(r == 1 ? "库存不足" : "不能重复参与秒杀");
        }

        // 2. 生成全局唯一订单ID
        long orderId = redisIdWorker.nextId("flash-order");

        // 3. 发送消息到 RabbitMQ 异步下单（仿照 share-parent 的 RabbitService）
        String message = flashSaleId + ":" + learnerId + ":" + orderId;
        String messageId = "mq:" + UUID.randomUUID().toString().replace("-", "");
        rabbitTemplate.convertAndSend(MqConst.EXCHANGE_FLASH, MqConst.ROUTING_FLASH_ORDER,
                message, msg -> {
                    msg.getMessageProperties().setMessageId(messageId);
                    return msg;
                });

        log.info("[秒杀服务] 发送秒杀下单消息: {}", message);
        return orderId;
    }

    /**
     * 异步创建秒杀订单（由 RabbitMQ 消费者调用）
     * 仿照黑马点评 createVoucherOrder 模式：
     * Redisson 分布式锁 → 一人一单校验 → MySQL 乐观锁扣库存 → 扣积分 → 写订单
     */
    @Override
    @Transactional
    public void createFlashOrder(Long flashSaleId, Long learnerId, Long orderId) {
        // 1. Redisson 分布式锁（一人一单，防止并发创建同一用户的多个订单）
        RLock lock = redissonClient.getLock("lock:flash:order:" + learnerId);
        boolean gotLock = lock.tryLock();
        if (!gotLock) {
            log.error("用户 {} 获取锁失败，可能重复下单", learnerId);
            return;
        }
        try {
            _createFlashOrder(flashSaleId, learnerId, orderId);
        } finally {
            lock.unlock();
        }
    }

    private void _createFlashOrder(Long flashSaleId, Long learnerId, Long orderId) {
        // 2. 一人一单（数据库兜底）
        long count = lambdaQuery()
                .eq(FlashSaleRecord::getFlashSaleId, flashSaleId)
                .eq(FlashSaleRecord::getLearnerId, learnerId)
                .count();
        if (count > 0) {
            log.error("用户 {} 已参与秒杀活动 {}", learnerId, flashSaleId);
            return;
        }

        // 3. 扣减数据库库存（乐观锁 CAS）
        boolean success = flashSaleService.lambdaUpdate()
                .setSql("stock = stock - 1")
                .eq(FlashSale::getId, flashSaleId)
                .gt(FlashSale::getStock, 0)
                .update();
        if (!success) {
            log.error("秒杀活动 {} 库存不足", flashSaleId);
            return;
        }

        FlashSale flashSale = flashSaleService.getById(flashSaleId);
        if (flashSale == null) return;

        // 4. 扣积分（秒杀价）
        CreditAccount account = accountService.openAccount(learnerId);
        int balanceBefore = account.getAvailableCredits();
        if (balanceBefore < flashSale.getFlashPrice()) {
            log.error("用户 {} 积分不足", learnerId);
            return;
        }
        account.setAvailableCredits(balanceBefore - flashSale.getFlashPrice());
        accountService.updateById(account);

        // 5. 创建订单（秒杀直接支付 = PAID）
        CreditOrder order = new CreditOrder();
        order.setId(orderId);
        order.setOrderNo("FS" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + orderId % 10000);
        order.setLearnerId(learnerId);
        order.setAccountId(account.getId());
        order.setProductId(flashSale.getProductId());
        order.setProductName(flashSale.getProductName() + " 【秒杀】");
        order.setCreditAmount(flashSale.getFlashPrice());
        order.setTotalAmount(flashSale.getFlashPrice());
        order.setItemCount(1);
        order.setOrderStatus("PAID");
        order.setPaidAt(LocalDateTime.now());
        order.setRemark("秒杀: " + flashSale.getProductName());
        orderService.save(order);

        // 6. 记录秒杀记录
        FlashSaleRecord record = new FlashSaleRecord();
        record.setFlashSaleId(flashSaleId);
        record.setProductId(flashSale.getProductId());
        record.setLearnerId(learnerId);
        record.setOrderId(orderId);
        save(record);

        log.info("[秒杀服务] 秒杀成功: 用户={}, 活动={}, 订单={}", learnerId, flashSaleId, orderId);
    }
}
