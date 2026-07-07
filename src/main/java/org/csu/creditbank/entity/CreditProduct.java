package org.csu.creditbank.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("credit_product")
public class CreditProduct extends BaseEntity {

    @TableId
    private Long id;
    private String productCode;
    private String productName;
    private String productType;
    private Integer creditPrice;
    private String description;
    private String imageUrl;
    private Integer stock;
    private Integer isActive;

    /** 累计销量 */
    private Integer sold;

    /** 评论数 */
    private Integer commentCount;
}
