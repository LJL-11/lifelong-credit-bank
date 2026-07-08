package org.csu.creditbank.controller.admin.mall;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.csu.creditbank.common.ApiResult;
import org.csu.creditbank.entity.FlashSale;
import org.csu.creditbank.service.FlashSaleService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/flash-sales")
public class FlashSaleAdminController {

    private final FlashSaleService flashSaleService;

    public FlashSaleAdminController(FlashSaleService flashSaleService) {
        this.flashSaleService = flashSaleService;
    }

    /** 管理端：分页列表 */
    @GetMapping
    public ApiResult<Page<FlashSale>> page(@RequestParam(defaultValue = "1") long current,
                                            @RequestParam(defaultValue = "10") long size) {
        return ApiResult.ok(flashSaleService.lambdaQuery()
                .orderByDesc(FlashSale::getCreatedAt)
                .page(Page.of(current, size)));
    }

    @GetMapping("/{id}")
    public ApiResult<FlashSale> detail(@PathVariable Long id) {
        return ApiResult.ok(flashSaleService.getById(id));
    }

    /** 新增秒杀活动 */
    @PostMapping
    public ApiResult<FlashSale> create(@RequestBody FlashSale sale) {
        // 默认状态
        if (sale.getStatus() == null || sale.getStatus().isEmpty()) {
            sale.setStatus("UPCOMING");
        }
        flashSaleService.save(sale);
        // 预加载库存到 Redis
        flashSaleService.preloadStock(sale.getId());
        return ApiResult.ok(sale);
    }

    /** 编辑秒杀活动 */
    @PutMapping("/{id}")
    public ApiResult<FlashSale> update(@PathVariable Long id, @RequestBody FlashSale sale) {
        sale.setId(id);
        flashSaleService.updateById(sale);
        flashSaleService.preloadStock(id);
        return ApiResult.ok(sale);
    }

    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        flashSaleService.removeById(id);
        return ApiResult.ok();
    }
}
