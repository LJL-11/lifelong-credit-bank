package org.csu.creditbank.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.csu.creditbank.common.ApiResult;
import org.csu.creditbank.dto.PlaceOrderRequest;
import org.csu.creditbank.entity.CreditOrder;
import org.csu.creditbank.service.CreditOrderService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/orders")
public class CreditOrderController {

    private final CreditOrderService orderService;

    public CreditOrderController(CreditOrderService orderService) {
        this.orderService = orderService;
    }

    /** 管理端：全部订单分页 */
    @GetMapping
    public ApiResult<Page<CreditOrder>> page(@RequestParam(defaultValue = "1") long current,
                                              @RequestParam(defaultValue = "10") long size) {
        return ApiResult.ok(orderService.page(Page.of(current, size)));
    }

    /** 学员订单列表 */
    @GetMapping("/learner/{learnerId}")
    public ApiResult<Page<CreditOrder>> byLearner(@PathVariable Long learnerId,
                                                   @RequestParam(defaultValue = "1") long current,
                                                   @RequestParam(defaultValue = "10") long size) {
        Page<CreditOrder> page = orderService.lambdaQuery()
                .eq(CreditOrder::getLearnerId, learnerId)
                .orderByDesc(CreditOrder::getCreatedAt)
                .page(Page.of(current, size));
        return ApiResult.ok(page);
    }

    @GetMapping("/{id}")
    public ApiResult<CreditOrder> getById(@PathVariable Long id) {
        return ApiResult.ok(orderService.getById(id));
    }

    /** 下单（冻结积分） */
    @PostMapping("/place")
    public ApiResult<CreditOrder> place(@Valid @RequestBody PlaceOrderRequest request) {
        return ApiResult.ok(orderService.placeOrder(request));
    }

    /** 确认支付（扣减冻结积分） */
    @PostMapping("/{id}/pay")
    public ApiResult<CreditOrder> pay(@PathVariable Long id) {
        return ApiResult.ok(orderService.confirmPay(id));
    }

    /** 发货 */
    @PostMapping("/{id}/deliver")
    public ApiResult<CreditOrder> deliver(@PathVariable Long id) {
        return ApiResult.ok(orderService.deliver(id));
    }

    /** 取消订单（解冻积分） */
    @PostMapping("/{id}/cancel")
    public ApiResult<CreditOrder> cancel(@PathVariable Long id) {
        return ApiResult.ok(orderService.cancel(id));
    }

    /** 退款 */
    @PostMapping("/{id}/refund")
    public ApiResult<CreditOrder> refund(@PathVariable Long id) {
        return ApiResult.ok(orderService.refund(id));
    }
}
