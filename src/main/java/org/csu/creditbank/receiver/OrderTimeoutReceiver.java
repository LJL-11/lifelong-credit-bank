package org.csu.creditbank.receiver;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.csu.creditbank.config.MqConst;
import org.csu.creditbank.entity.CreditOrder;
import org.csu.creditbank.service.CreditOrderService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class OrderTimeoutReceiver {

    private final CreditOrderService orderService;
    private final StringRedisTemplate redis;

    public OrderTimeoutReceiver(CreditOrderService orderService,
                                 StringRedisTemplate redis) {
        this.orderService = orderService;
        this.redis = redis;
    }

    @RabbitListener(queues = MqConst.QUEUE_ORDER_TIMEOUT)
    public void handleOrderTimeout(String content, Message message, Channel channel) {
        log.info("[订单超时] 收到超时取消消息: {}", content);
        try {
            Long orderId = Long.valueOf(content);
            String dedupKey = "order:timeout:dedup:" + orderId;
            Boolean isNew = redis.opsForValue().setIfAbsent(dedupKey, "1", 24, TimeUnit.HOURS);
            if (Boolean.FALSE.equals(isNew)) {
                log.info("[订单超时] 重复消息，跳过: {}", orderId);
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                return;
            }

            CreditOrder order = orderService.getById(orderId);
            if (order == null) {
                log.warn("[订单超时] 订单不存在: {}", orderId);
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                return;
            }
            if (!"PENDING".equals(order.getOrderStatus())) {
                log.info("[订单超时] 订单已处理，状态={}，跳过: {}", order.getOrderStatus(), orderId);
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                return;
            }

            orderService.cancel(orderId);
            log.info("[订单超时] 订单 {} 超时自动取消成功", orderId);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("[订单超时] 处理失败: {}", content, e);
            try {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            } catch (Exception ignored) {}
        }
    }
}
