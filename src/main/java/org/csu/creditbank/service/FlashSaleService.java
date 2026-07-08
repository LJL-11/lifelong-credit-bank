package org.csu.creditbank.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.csu.creditbank.entity.FlashSale;

import java.util.List;

public interface FlashSaleService extends IService<FlashSale> {

    /** 获取正在进行的秒杀活动（从 Redis 读取实时库存） */
    List<FlashSale> getActiveSales();

    /** 预加载库存到 Redis */
    void preloadStock(Long flashSaleId);
}
