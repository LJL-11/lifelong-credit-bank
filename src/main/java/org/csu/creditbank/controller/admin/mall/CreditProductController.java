package org.csu.creditbank.controller.admin.mall;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.csu.creditbank.common.ApiResult;
import org.csu.creditbank.entity.CreditProduct;
import org.csu.creditbank.service.CreditProductService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/products")
public class CreditProductController {

    private final CreditProductService productService;

    public CreditProductController(CreditProductService productService) {
        this.productService = productService;
    }

    /** 管理端：分页列表（含下架商品） */
    @GetMapping
    public ApiResult<Page<CreditProduct>> page(@RequestParam(defaultValue = "1") long current,
                                                @RequestParam(defaultValue = "10") long size) {
        return ApiResult.ok(productService.page(Page.of(current, size)));
    }

    /** 商城端：只返回上架且有库存的商品 */
    @GetMapping("/active")
    public ApiResult<Page<CreditProduct>> active(@RequestParam(defaultValue = "1") long current,
                                                  @RequestParam(defaultValue = "10") long size) {
        LambdaQueryWrapper<CreditProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CreditProduct::getIsActive, 1)
               .and(w -> w.eq(CreditProduct::getStock, -1).or().gt(CreditProduct::getStock, 0));
        return ApiResult.ok(productService.page(Page.of(current, size), wrapper));
    }

    @GetMapping("/{id}")
    public ApiResult<CreditProduct> getById(@PathVariable Long id) {
        return ApiResult.ok(productService.getById(id));
    }

    @PostMapping
    public ApiResult<CreditProduct> create(@Valid @RequestBody CreditProduct product) {
        productService.save(product);
        return ApiResult.ok(product);
    }

    @PutMapping("/{id}")
    public ApiResult<CreditProduct> update(@PathVariable Long id, @RequestBody CreditProduct product) {
        product.setId(id);
        productService.updateById(product);
        return ApiResult.ok(product);
    }

    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        productService.removeById(id);
        return ApiResult.ok();
    }
}
