package org.csu.creditbank.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.csu.creditbank.entity.FlashSaleRecord;

public interface FlashSaleOrderService extends IService<FlashSaleRecord> {

    /** 秒杀优惠券（入口：Lua 脚本原子判库存+去重 → 阻塞队列异步下单） */
    long seckill(Long flashSaleId, Long learnerId);

    /** 异步创建订单（被阻塞队列消费者调用） */
    void createFlashOrder(Long flashSaleId, Long learnerId, Long orderId);
}
