package org.csu.creditbank.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

/**
 * 正常订单超时取消 DLQ 配置：
 *   order.timeout.task (TTL 15min, dead-letter → order.timeout)
 *   order.timeout (死信目标，消费者在此取消超时未支付订单)
 */
@Configuration
public class OrderMqConfig {

    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(MqConst.EXCHANGE_ORDER, true, false);
    }

    /** 延时队列：TTL 15 分钟后自动死信 */
    @Bean
    public Queue orderTimeoutTaskQueue() {
        HashMap<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", MqConst.EXCHANGE_ORDER);
        args.put("x-dead-letter-routing-key", MqConst.ROUTING_ORDER_TIMEOUT);
        args.put("x-message-ttl", 15 * 60 * 1000);
        return new Queue(MqConst.QUEUE_ORDER_TIMEOUT_TASK, true, false, false, args);
    }

    /** 死信目标队列 */
    @Bean
    public Queue orderTimeoutQueue() {
        return new Queue(MqConst.QUEUE_ORDER_TIMEOUT, true, false, false);
    }

    @Bean
    public Binding orderTimeoutTaskBinding() {
        return BindingBuilder.bind(orderTimeoutTaskQueue())
                .to(orderExchange())
                .with(MqConst.QUEUE_ORDER_TIMEOUT_TASK);
    }

    @Bean
    public Binding orderTimeoutBinding() {
        return BindingBuilder.bind(orderTimeoutQueue())
                .to(orderExchange())
                .with(MqConst.ROUTING_ORDER_TIMEOUT);
    }
}
