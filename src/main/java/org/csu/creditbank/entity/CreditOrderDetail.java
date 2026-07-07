package org.csu.creditbank.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("credit_order_detail")
public class CreditOrderDetail extends BaseEntity {

    @TableId
    private Long id;

    /** 订单ID */
    private Long orderId;

    /** 订单号 */
    private String orderNo;

    /** 商品ID */
    private Long productId;

    /** 商品名称快照 */
    private String productName;

    /** 兑换时积分单价快照 */
    private Integer creditPrice;

    /** 购买数量 */
    private Integer num;

    /** 小计积分 */
    private Integer amount;

    /** 商品图片快照 */
    private String imageUrl;
}
