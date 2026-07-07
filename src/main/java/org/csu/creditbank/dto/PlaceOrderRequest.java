package org.csu.creditbank.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class PlaceOrderRequest {

    private Long learnerId;

    /** 直接下单：商品ID */
    private Long productId;

    /** 直接下单：数量 */
    @Min(1)
    private Integer quantity = 1;

    /** 购物车下单：选中的购物车项ID列表 */
    private List<Long> cartIds;

    private String remark;
}
