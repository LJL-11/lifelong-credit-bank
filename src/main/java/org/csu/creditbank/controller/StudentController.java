package org.csu.creditbank.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.csu.creditbank.common.ApiResult;
import org.csu.creditbank.common.BusinessException;
import org.csu.creditbank.dto.PlaceOrderRequest;
import org.csu.creditbank.entity.CreditAccount;
import org.csu.creditbank.entity.CreditOrder;
import org.csu.creditbank.entity.CreditProduct;
import org.csu.creditbank.entity.Learner;
import org.csu.creditbank.service.CreditAccountService;
import org.csu.creditbank.service.CourseService;
import org.csu.creditbank.entity.Course;
import org.csu.creditbank.service.CreditOrderService;
import org.csu.creditbank.service.CreditProductService;
import org.csu.creditbank.service.LearnerService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    private final LearnerService learnerService;
    private final CreditAccountService accountService;
    private final CreditProductService productService;
    private final CreditOrderService orderService;
    private final CourseService courseService;

    public StudentController(LearnerService learnerService,
                             CourseService courseService,
                             CreditAccountService accountService,
                             CreditProductService productService,
                             CreditOrderService orderService) {
        this.learnerService = learnerService;
        this.accountService = accountService;
        this.productService = productService;
        this.orderService = orderService;
        this.courseService = courseService;
    }

    /** 获取当前学员个人信息 */
    @GetMapping("/profile")
    public ApiResult<Learner> profile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResult.ok(learnerService.getById(userId));
    }

    /** 更新当前学员个人信息 */
    @PutMapping("/profile")
    public ApiResult<Learner> updateProfile(@RequestBody Learner update, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        update.setId(userId);
        // 不允许学员自己修改 role 和 status
        update.setRole(null);
        update.setStatus(null);
        update.setPassword(null);
        update.setUsername(null);
        learnerService.updateById(update);
        return ApiResult.ok(learnerService.getById(userId));
    }

    /** 查询自己的积分账户 */
    @GetMapping("/account")
    public ApiResult<CreditAccount> account(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        CreditAccount account = accountService.lambdaQuery()
                .eq(CreditAccount::getLearnerId, userId).one();
        if (account == null) {
            throw new BusinessException("积分账户未开通，请联系管理员");
        }
        return ApiResult.ok(account);
    }

    /** 开通积分账户 */
    @PostMapping("/account/open")
    public ApiResult<CreditAccount> openAccount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResult.ok(accountService.openAccount(userId));
    }

    /** 上架商品列表 */
    @GetMapping("/products")
    public ApiResult<Page<CreditProduct>> products(@RequestParam(defaultValue = "1") long current,
                                                    @RequestParam(defaultValue = "10") long size) {
        LambdaQueryWrapper<CreditProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CreditProduct::getIsActive, 1)
               .and(w -> w.eq(CreditProduct::getStock, -1).or().gt(CreditProduct::getStock, 0));
        return ApiResult.ok(productService.page(Page.of(current, size), wrapper));
    }

    /** 下单（learnerId 从 token 取，支持单商品和购物车两种模式） */
    @PostMapping("/orders/place")
    public ApiResult<?> place(@Valid @RequestBody PlaceOrderRequest req,
                               HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        req.setLearnerId(userId);
        if (req.getCartIds() != null && !req.getCartIds().isEmpty()) {
            return ApiResult.ok(orderService.placeOrderFromCart(req));
        }
        return ApiResult.ok(orderService.placeOrder(req));
    }

    /** 我的订单列表 */
    @GetMapping("/orders")
    public ApiResult<Page<CreditOrder>> myOrders(@RequestParam(defaultValue = "1") long current,
                                                  @RequestParam(defaultValue = "10") long size,
                                                  HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Page<CreditOrder> page = orderService.lambdaQuery()
                .eq(CreditOrder::getLearnerId, userId)
                .orderByDesc(CreditOrder::getCreatedAt)
                .page(Page.of(current, size));
        return ApiResult.ok(page);
    }

    /** 支付 */
    @PostMapping("/orders/{id}/pay")
    public ApiResult<CreditOrder> pay(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        CreditOrder order = orderService.getById(id);
        if (order == null || !order.getLearnerId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        return ApiResult.ok(orderService.confirmPay(id));
    }

    /** 取消订单 */
    @PostMapping("/orders/{id}/cancel")
    public ApiResult<CreditOrder> cancel(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        CreditOrder order = orderService.getById(id);
        if (order == null || !order.getLearnerId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        return ApiResult.ok(orderService.cancel(id));
    }

    /** 学员积分统计 */
    @GetMapping("/stats")
    public ApiResult<Map<String, Object>> stats(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        CreditAccount account = accountService.lambdaQuery()
                .eq(CreditAccount::getLearnerId, userId).one();
        long orderCount = orderService.lambdaQuery()
                .eq(CreditOrder::getLearnerId, userId).count();
        return ApiResult.ok(Map.of(
                "account", account,
                "totalOrders", orderCount
        ));
    }
}
