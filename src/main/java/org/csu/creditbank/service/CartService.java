package org.csu.creditbank.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.csu.creditbank.dto.AddToCartRequest;
import org.csu.creditbank.entity.Cart;

import java.util.List;

public interface CartService extends IService<Cart> {

    /** 加入购物车：同商品则叠加数量 */
    Cart addToCart(Long learnerId, AddToCartRequest request);

    /** 我的购物车列表 */
    List<Cart> myCarts(Long learnerId);

    /** 更新数量 */
    void updateNum(Long cartId, Integer num);

    /** 批量删除（下单后清理） */
    void removeByIds(List<Long> ids);

    /** 获取购物车总积分 */
    int totalCredits(Long learnerId, List<Long> cartIds);
}
