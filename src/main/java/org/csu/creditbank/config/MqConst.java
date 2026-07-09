package org.csu.creditbank.config;

public class MqConst {

    /** 秒杀订单交换机 */
    public static final String EXCHANGE_FLASH = "credit.flash";

    /** 秒杀下单队列 */
    public static final String QUEUE_FLASH_ORDER = "credit.flash.order";

    /** 秒杀下单路由键 */
    public static final String ROUTING_FLASH_ORDER = "credit.flash.order";

    /** 秒杀超时取消队列（死信） */
    public static final String QUEUE_FLASH_TIMEOUT = "credit.flash.timeout";

    /** 秒杀超时取消路由键 */
    public static final String ROUTING_FLASH_TIMEOUT = "credit.flash.timeout";

    // ==================== 正常订单超时取消 ====================

    /** 订单交换机 */
    public static final String EXCHANGE_ORDER = "credit.order";

    /** 订单延时队列（TTL 15min → 死信） */
    public static final String QUEUE_ORDER_TIMEOUT_TASK = "credit.order.timeout.task";

    /** 订单超时取消队列（死信目标） */
    public static final String QUEUE_ORDER_TIMEOUT = "credit.order.timeout";

    /** 路由键 */
    public static final String ROUTING_ORDER_TIMEOUT = "credit.order.timeout";
}
