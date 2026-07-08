package org.csu.creditbank.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.csu.creditbank.common.ApiResult;
import org.csu.creditbank.entity.FlashSale;
import org.csu.creditbank.service.FlashSaleOrderService;
import org.csu.creditbank.service.FlashSaleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flash")
public class FlashSaleController {

    private final FlashSaleService flashSaleService;
    private final FlashSaleOrderService flashSaleOrderService;

    public FlashSaleController(FlashSaleService flashSaleService,
                                FlashSaleOrderService flashSaleOrderService) {
        this.flashSaleService = flashSaleService;
        this.flashSaleOrderService = flashSaleOrderService;
    }

    /** 获取活跃秒杀活动列表（含 Redis 实时库存） */
    @GetMapping("/active")
    public ApiResult<List<FlashSale>> activeSales() {
        return ApiResult.ok(flashSaleService.getActiveSales());
    }

    /** 秒杀下单入口 */
    @PostMapping("/seckill/{flashSaleId}")
    public ApiResult<Long> seckill(@PathVariable Long flashSaleId,
                                    HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        long orderId = flashSaleOrderService.seckill(flashSaleId, userId);
        return ApiResult.ok(orderId);
    }
}
