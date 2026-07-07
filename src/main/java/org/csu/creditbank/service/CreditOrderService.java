package org.csu.creditbank.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.csu.creditbank.dto.PlaceOrderRequest;
import org.csu.creditbank.entity.CreditOrder;

import java.util.List;
public interface CreditOrderService extends IService<CreditOrder> {

    /** 直接下单（单商品） + 冻结积分 */
    CreditOrder placeOrder(PlaceOrderRequest request);

    /** 购物车下单（多商品） + 冻结积分，返回订单ID列表 */
    List<CreditOrder> placeOrderFromCart(PlaceOrderRequest request);

    /** 确认支付：冻结积分 → 正式扣减 */
    CreditOrder confirmPay(Long orderId);

    /** 发货 */
    CreditOrder deliver(Long orderId);

    /** 取消订单（未支付时解冻） */
    CreditOrder cancel(Long orderId);

    /** 退款（已支付后退还积分） */
    CreditOrder refund(Long orderId);
}
