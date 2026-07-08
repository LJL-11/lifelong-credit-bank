package org.csu.creditbank.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.csu.creditbank.entity.FlashSale;
import org.csu.creditbank.mapper.FlashSaleMapper;
import org.csu.creditbank.service.FlashSaleService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FlashSaleServiceImpl extends ServiceImpl<FlashSaleMapper, FlashSale> implements FlashSaleService {

    private final StringRedisTemplate stringRedisTemplate;

    public FlashSaleServiceImpl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public List<FlashSale> getActiveSales() {
        // 查状态为 ACTIVE 且在时间范围内的活动
        List<FlashSale> list = lambdaQuery()
                .eq(FlashSale::getStatus, "ACTIVE")
                .le(FlashSale::getBeginTime, java.time.LocalDateTime.now())
                .ge(FlashSale::getEndTime, java.time.LocalDateTime.now())
                .list();
        for (FlashSale sale : list) {
            String stockKey = "flash:stock:" + sale.getId();
            String stockStr = stringRedisTemplate.opsForValue().get(stockKey);
            if (stockStr != null) {
                sale.setRedisStock(Integer.parseInt(stockStr));
            } else {
                // 首次访问时预加载
                preloadStock(sale.getId());
                sale.setRedisStock(sale.getStock());
            }
        }
        return list;
    }

    @Override
    public void preloadStock(Long flashSaleId) {
        FlashSale sale = getById(flashSaleId);
        if (sale != null) {
            String stockKey = "flash:stock:" + flashSaleId;
            stringRedisTemplate.opsForValue().setIfAbsent(stockKey, String.valueOf(sale.getStock()));
        }
    }
}
