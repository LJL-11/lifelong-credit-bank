package org.csu.creditbank.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("credit_order")
public class CreditOrder extends BaseEntity {

    @TableId
    private Long id;
    private String orderNo;
    private Long learnerId;
    private Long accountId;
    private Long productId;
    private String productName;
    private Integer creditAmount;
    private String orderStatus;
    private LocalDateTime paidAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime cancelledAt;
    private String remark;

    /** 订单总积分 */
    private Integer totalAmount;

    /** 商品件数 */
    private Integer itemCount;

    /** 批次号（购物车结算多个订单共享） */
    private String batchNo;

    /** 交易关闭时间 */
    private LocalDateTime closeTime;
}
