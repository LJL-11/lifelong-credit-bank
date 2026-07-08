package org.csu.creditbank.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("flash_sale_record")
public class FlashSaleRecord extends BaseEntity {

    @TableId
    private Long id;
    private Long flashSaleId;
    private Long productId;
    private Long learnerId;
    private Long orderId;
}
