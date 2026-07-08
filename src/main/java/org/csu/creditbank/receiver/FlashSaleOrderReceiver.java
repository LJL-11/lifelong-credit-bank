package org.csu.creditbank.receiver;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.csu.creditbank.config.MqConst;
import org.csu.creditbank.service.FlashSaleOrderService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class FlashSaleOrderReceiver {

    private final FlashSaleOrderService flashSaleOrderService;
    private final StringRedisTemplate stringRedisTemplate;

    public FlashSaleOrderReceiver(FlashSaleOrderService flashSaleOrderService,
                                   StringRedisTemplate stringRedisTemplate) {
        this.flashSaleOrderService = flashSaleOrderService;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 秒杀下单消费者 —— 监听秒杀下单队列（队列由 FlashSaleMqConfig 声明）
     * 仿照 share-parent 的 @RabbitListener + 手动应答 + Redis 防重
     */
    @RabbitListener(queues = MqConst.QUEUE_FLASH_ORDER)
    public void handleFlashOrder(String content, Message message, Channel channel) {
        log.info("[秒杀服务] 收到秒杀下单消息: {}", content);

        String messageId = message.getMessageProperties().getMessageId();
        // 防重：同一消息只处理一次
        String dedupKey = "flash:dedup:" + messageId;
        Boolean isNew = stringRedisTemplate.opsForValue()
                .setIfAbsent(dedupKey, "1", 1, TimeUnit.HOURS);
        if (Boolean.FALSE.equals(isNew)) {
            log.info("重复消息: {}", messageId);
            try { channel.basicAck(message.getMessageProperties().getDeliveryTag(), false); } catch (Exception ignored) {}
            return;
        }

        try {
            String[] parts = content.split(":");
            Long flashSaleId = Long.valueOf(parts[0]);
            Long learnerId = Long.valueOf(parts[1]);
            Long orderId = Long.valueOf(parts[2]);

            flashSaleOrderService.createFlashOrder(flashSaleId, learnerId, orderId);

            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            log.info("[秒杀服务] 订单创建成功: flashSaleId={}, learnerId={}, orderId={}",
                    flashSaleId, learnerId, orderId);
        } catch (Exception e) {
            log.error("[秒杀服务] 订单创建失败: {}", content, e);
            stringRedisTemplate.delete(dedupKey);
            try { channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true); } catch (Exception ignored) {}
        }
    }

    /**
     * 超时取消消费者 —— 监听死信队列（队列由 FlashSaleMqConfig 声明）
     */
    @RabbitListener(queues = MqConst.QUEUE_FLASH_TIMEOUT)
    public void handleTimeout(String content, Message message, Channel channel) {
        log.info("[秒杀服务] 收到超时取消消息: {}", content);
        try {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("[秒杀服务] 超时取消处理失败", e);
        }
    }
}
