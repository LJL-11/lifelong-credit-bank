package org.csu.creditbank.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.csu.creditbank.common.ApiResult;
import org.csu.creditbank.dto.AddToCartRequest;
import org.csu.creditbank.entity.Cart;
import org.csu.creditbank.service.CartService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student/cart")
public class StudentCartController {

    private final CartService cartService;

    public StudentCartController(CartService cartService) {
        this.cartService = cartService;
    }

    /** 加入购物车 */
    @PostMapping
    public ApiResult<Cart> add(@Valid @RequestBody AddToCartRequest request,
                                HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        return ApiResult.ok(cartService.addToCart(userId, request));
    }

    /** 我的购物车列表 */
    @GetMapping
    public ApiResult<List<Cart>> list(HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        return ApiResult.ok(cartService.myCarts(userId));
    }

    /** 更新数量 */
    @PutMapping("/{id}")
    public ApiResult<Void> updateNum(@PathVariable Long id, @RequestBody Map<String, Integer> body,
                                      HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        Cart cart = cartService.getById(id);
        if (cart == null || !cart.getLearnerId().equals(userId)) {
            return ApiResult.fail("购物车项不存在");
        }
        cartService.updateNum(id, body.get("num"));
        return ApiResult.ok();
    }

    /** 删除购物车项 */
    @DeleteMapping("/{id}")
    public ApiResult<Void> remove(@PathVariable Long id, HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        Cart cart = cartService.getById(id);
        if (cart == null || !cart.getLearnerId().equals(userId)) {
            return ApiResult.fail("购物车项不存在");
        }
        cartService.removeById(id);
        return ApiResult.ok();
    }

    /** 批量删除 */
    @DeleteMapping("/batch")
    public ApiResult<Void> removeBatch(@RequestBody Map<String, List<Long>> body,
                                        HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        List<Long> ids = body.get("ids");
        // 校验所有项都属于当前用户
        if (ids != null && !ids.isEmpty()) {
            long count = cartService.lambdaQuery()
                    .eq(Cart::getLearnerId, userId)
                    .in(Cart::getId, ids)
                    .count();
            if (count != ids.size()) {
                return ApiResult.fail("存在不属于您的购物车项");
            }
            cartService.removeByIds(ids);
        }
        return ApiResult.ok();
    }

    /** 计算选中项的积分总额 */
    @PostMapping("/calc")
    public ApiResult<Map<String, Object>> calc(@RequestBody Map<String, List<Long>> body,
                                                HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        List<Long> ids = body.get("ids");
        int total = cartService.totalCredits(userId, ids);
        return ApiResult.ok(Map.of("totalCredits", total, "count", ids != null ? ids.size() : 0));
    }
}
