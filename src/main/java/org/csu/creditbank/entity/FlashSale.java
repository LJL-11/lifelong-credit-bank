package org.csu.creditbank.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("flash_sale")
public class FlashSale extends BaseEntity {

    @TableId
    private Long id;
    private Long productId;
    private String productName;
    private Integer originPrice;
    private Integer flashPrice;
    private Integer stock;
    private LocalDateTime beginTime;
    private LocalDateTime endTime;
    private String status;

    /** 秒杀库存（Redis 中读取，非 DB 字段） */
    @TableField(exist = false)
    private Integer redisStock;
}
