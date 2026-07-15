package org.csu.creditbank.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.csu.creditbank.common.BusinessException;
import org.csu.creditbank.config.MqConst;
import org.csu.creditbank.dto.PlaceOrderRequest;
import org.csu.creditbank.entity.*;
import org.csu.creditbank.mapper.CreditOrderMapper;
import org.csu.creditbank.service.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class CreditOrderServiceImpl extends ServiceImpl<CreditOrderMapper, CreditOrder> implements CreditOrderService {

    private final CreditProductService productService;
    private final CreditAccountService accountService;
    private final CreditTransactionService transactionService;
    private final CartService cartService;
    private final CreditOrderDetailService detailService;
    private final CourseEnrollmentService enrollmentService;
    private final RabbitTemplate rabbitTemplate;

    public CreditOrderServiceImpl(CreditProductService productService,
                                  CreditAccountService accountService,
                                  CreditTransactionService transactionService,
                                  CartService cartService,
                                  CreditOrderDetailService detailService,
                                  CourseEnrollmentService enrollmentService,
                                  RabbitTemplate rabbitTemplate) {
        this.productService = productService;
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.cartService = cartService;
        this.detailService = detailService;
        this.enrollmentService = enrollmentService;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    @Transactional
    public CreditOrder placeOrder(PlaceOrderRequest request) {
        CreditProduct product = productService.getById(request.getProductId());
        if (product == null || product.getIsActive() != 1) {
            throw new BusinessException("商品不存在或已下架");
        }
        if (product.getStock() == 0) {
            throw new BusinessException("商品库存不足");
        }

        int totalAmount = product.getCreditPrice() * request.getQuantity();

        // 只校验账户存在且可用积分充足，不冻结
        CreditAccount account = accountService.openAccount(request.getLearnerId());
        if (account.getAvailableCredits() < totalAmount) {
            throw new BusinessException("可用积分不足");
        }

        // 扣库存
        if (product.getStock() > 0) {
            product.setStock(product.getStock() - request.getQuantity());
            productService.updateById(product);
        }

        CreditOrder order = buildOrder(request.getLearnerId(), account.getId(), totalAmount, request.getQuantity(), null, request.getRemark());
        order.setProductId(product.getId());
        order.setProductName(product.getProductName());
        save(order);

        CreditOrderDetail detail = new CreditOrderDetail();
        detail.setOrderId(order.getId());
        detail.setOrderNo(order.getOrderNo());
        detail.setProductId(product.getId());
        detail.setProductName(product.getProductName());
        detail.setCreditPrice(product.getCreditPrice());
        detail.setNum(request.getQuantity());
        detail.setAmount(totalAmount);
        detail.setImageUrl(product.getImageUrl());
        detailService.save(detail);

        // 发送 15 分钟超时取消消息
        sendTimeoutMessage(order.getId());

        return order;
    }

    /** 发送订单超时延时消息（15 分钟后自动取消未支付订单） */
    private void sendTimeoutMessage(Long orderId) {
        rabbitTemplate.convertAndSend(MqConst.EXCHANGE_ORDER,
                MqConst.QUEUE_ORDER_TIMEOUT_TASK, orderId.toString());
    }

    @Override
    @Transactional
    public List<CreditOrder> placeOrderFromCart(PlaceOrderRequest request) {
        List<Cart> cartItems = cartService.lambdaQuery()
                .eq(Cart::getLearnerId, request.getLearnerId())
                .in(Cart::getId, request.getCartIds())
                .list();
        if (cartItems.isEmpty()) throw new BusinessException("购物车选项为空");

        int totalAmount = cartItems.stream().mapToInt(c -> c.getCreditPrice() * c.getNum()).sum();

        // 只校验可用积分充足，不冻结
        CreditAccount account = accountService.openAccount(request.getLearnerId());
        if (account.getAvailableCredits() < totalAmount) {
            throw new BusinessException("可用积分不足，还差 " + (totalAmount - account.getAvailableCredits()) + " 积分");
        }

        String batchNo = "B" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + ThreadLocalRandom.current().nextInt(100, 999);
        List<CreditOrder> orders = new ArrayList<>();

        for (Cart cartItem : cartItems) {
            CreditProduct product = productService.getById(cartItem.getProductId());
            if (product == null || product.getIsActive() != 1) continue;
            if (product.getStock() == 0) continue;

            int itemAmount = cartItem.getCreditPrice() * cartItem.getNum();

            if (product.getStock() > 0) {
                product.setStock(product.getStock() - cartItem.getNum());
                productService.updateById(product);
            }

            CreditOrder order = buildOrder(request.getLearnerId(), account.getId(),
                    itemAmount, cartItem.getNum(), batchNo, request.getRemark());
            order.setProductId(product.getId());
            order.setProductName(cartItem.getProductName());
            save(order);

            CreditOrderDetail detail = new CreditOrderDetail();
            detail.setOrderId(order.getId());
            detail.setOrderNo(order.getOrderNo());
            detail.setProductId(product.getId());
            detail.setProductName(cartItem.getProductName());
            detail.setCreditPrice(cartItem.getCreditPrice());
            detail.setNum(cartItem.getNum());
            detail.setAmount(itemAmount);
            detail.setImageUrl(product.getImageUrl());
            detailService.save(detail);

            orders.add(order);
        }

        cartService.removeByIds(request.getCartIds());

        // 每个订单发送 15 分钟超时取消消息
        for (CreditOrder o : orders) {
            sendTimeoutMessage(o.getId());
        }
        return orders;
    }

    @Override
    @Transactional
    public CreditOrder confirmPay(Long orderId) {
        CreditOrder order = getById(orderId);
        if (order == null) throw new BusinessException("订单不存在");
        if (!"PENDING".equals(order.getOrderStatus()))
            throw new BusinessException("订单状态不允许支付，当前状态: " + order.getOrderStatus());

        // 直接扣积分（totalAmount 有 DB NOT NULL 兜底，加 null 保护防 NPE）
        int totalAmount = order.getTotalAmount() != null ? order.getTotalAmount() : 0;
        if (totalAmount <= 0) {
            throw new BusinessException("订单金额异常");
        }
        CreditAccount account = accountService.openAccount(order.getLearnerId());
        int balanceBefore = account.getAvailableCredits();
        if (balanceBefore < totalAmount) {
            throw new BusinessException("积分不足，当前可用: " + balanceBefore + "，需要: " + totalAmount);
        }
        account.setAvailableCredits(balanceBefore - totalAmount);
        account.setTotalCredits(account.getTotalCredits() - totalAmount);
        accountService.updateById(account);

        order.setOrderStatus("PAID");
        order.setPaidAt(LocalDateTime.now());
        updateById(order);

        // 销量+1
        List<CreditOrderDetail> details = detailService.lambdaQuery()
                .eq(CreditOrderDetail::getOrderId, orderId).list();
        for (CreditOrderDetail d : details) {
            CreditProduct product = productService.getById(d.getProductId());
            if (product != null) {
                product.setSold((product.getSold() == null ? 0 : product.getSold()) + d.getNum());
                productService.updateById(product);
            }
        }

        saveTransaction(order.getLearnerId(), order.getAccountId(), "CONSUME",
                order.getTotalAmount(), balanceBefore, account.getAvailableCredits(),
                "ORDER_PAY", order.getOrderNo(), "支付扣减");

        grantPurchasedCourses(order, details);

        return order;
    }

    @Override
    @Transactional
    public CreditOrder deliver(Long orderId) {
        CreditOrder order = getById(orderId);
        if (order == null) throw new BusinessException("订单不存在");
        if (!"PAID".equals(order.getOrderStatus()))
            throw new BusinessException("订单状态不允许发货，当前状态: " + order.getOrderStatus());
        order.setOrderStatus("DELIVERED");
        order.setDeliveredAt(LocalDateTime.now());
        updateById(order);
        return order;
    }

    @Override
    @Transactional
    public CreditOrder cancel(Long orderId) {
        CreditOrder order = getById(orderId);
        if (order == null) throw new BusinessException("订单不存在");
        if (!"PENDING".equals(order.getOrderStatus()))
            throw new BusinessException("仅待支付订单可取消，当前状态: " + order.getOrderStatus());

        // 恢复库存
        List<CreditOrderDetail> details = detailService.lambdaQuery()
                .eq(CreditOrderDetail::getOrderId, orderId).list();
        for (CreditOrderDetail d : details) {
            CreditProduct product = productService.getById(d.getProductId());
            if (product != null && product.getStock() >= 0) {
                product.setStock(product.getStock() + d.getNum());
                productService.updateById(product);
            }
        }

        order.setOrderStatus("CANCELLED");
        order.setCancelledAt(LocalDateTime.now());
        order.setCloseTime(LocalDateTime.now());
        updateById(order);

        return order;
    }

    @Override
    @Transactional
    public CreditOrder refund(Long orderId) {
        CreditOrder order = getById(orderId);
        if (order == null) throw new BusinessException("订单不存在");
        if (!"PAID".equals(order.getOrderStatus()) && !"DELIVERED".equals(order.getOrderStatus()))
            throw new BusinessException("仅已支付/已发货订单可退款");

        // 退还积分
        CreditAccount account = accountService.openAccount(order.getLearnerId());
        int balanceBefore = account.getAvailableCredits();
        account.setAvailableCredits(balanceBefore + order.getTotalAmount());
        account.setTotalCredits(account.getTotalCredits() + order.getTotalAmount());
        accountService.updateById(account);

        order.setOrderStatus("REFUNDED");
        order.setCloseTime(LocalDateTime.now());
        updateById(order);

        saveTransaction(order.getLearnerId(), order.getAccountId(), "REFUND",
                order.getTotalAmount(), balanceBefore, account.getAvailableCredits(),
                "ORDER_REFUND", order.getOrderNo(), "订单退款");

        return order;
    }

    private void grantPurchasedCourses(CreditOrder order, List<CreditOrderDetail> details) {
        for (CreditOrderDetail detail : details) {
            enrollmentService.grantPurchasedCourse(order.getLearnerId(), detail.getProductId(), "购买课程商品自动开通");
        }
    }

    private CreditOrder buildOrder(Long learnerId, Long accountId, int amount, int count, String batchNo, String remark) {
        CreditOrder order = new CreditOrder();
        order.setDeleted(0); // @TableLogic: 必须显式设0，否则 null 查不到
        order.setOrderNo(generateOrderNo());
        order.setLearnerId(learnerId);
        order.setAccountId(accountId);
        order.setProductId(0L);
        order.setProductName("多商品订单");
        order.setCreditAmount(amount);
        order.setTotalAmount(amount);
        order.setItemCount(count);
        order.setBatchNo(batchNo);
        order.setOrderStatus("PENDING");
        order.setRemark(remark);
        return order;
    }

    private String generateOrderNo() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "CO" + datePart + random;
    }

    private void saveTransaction(Long learnerId, Long accountId, String type,
                                  int amount, int balanceBefore, int balanceAfter,
                                  String sourceType, String sourceNo, String remark) {
        CreditTransaction t = new CreditTransaction();
        t.setLearnerId(learnerId);
        t.setAccountId(accountId);
        t.setTransactionType(type);
        t.setAmount(amount);
        t.setBalanceBefore(balanceBefore);
        t.setBalanceAfter(balanceAfter);
        t.setSourceType(sourceType);
        t.setSourceNo(sourceNo);
        t.setRemark(remark);
        transactionService.save(t);
    }
}
