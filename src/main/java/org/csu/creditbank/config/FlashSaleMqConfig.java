package org.csu.creditbank.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

@Configuration
public class FlashSaleMqConfig {

    /** 声明秒杀交换机 */
    @Bean
    public DirectExchange flashExchange() {
        return new DirectExchange(MqConst.EXCHANGE_FLASH, true, false);
    }

    /** 秒杀下单队列（带死信：超时未处理自动取消） */
    @Bean
    public Queue flashOrderQueue() {
        HashMap<String, Object> args = new HashMap<>();
        // 死信交换机
        args.put("x-dead-letter-exchange", MqConst.EXCHANGE_FLASH);
        // 死信路由键 → 到超时取消队列
        args.put("x-dead-letter-routing-key", MqConst.ROUTING_FLASH_TIMEOUT);
        // 消息 TTL: 15分钟 = 900000ms
        args.put("x-message-ttl", 15 * 60 * 1000);
        return new Queue(MqConst.QUEUE_FLASH_ORDER, true, false, false, args);
    }

    /** 秒杀下单绑定 */
    @Bean
    public Binding flashOrderBinding() {
        return BindingBuilder.bind(flashOrderQueue())
                .to(flashExchange())
                .with(MqConst.ROUTING_FLASH_ORDER);
    }

    /** 超时取消队列（死信目标队列） */
    @Bean
    public Queue flashTimeoutQueue() {
        return new Queue(MqConst.QUEUE_FLASH_TIMEOUT, true, false, false);
    }

    /** 超时取消绑定 */
    @Bean
    public Binding flashTimeoutBinding() {
        return BindingBuilder.bind(flashTimeoutQueue())
                .to(flashExchange())
                .with(MqConst.ROUTING_FLASH_TIMEOUT);
    }
}
