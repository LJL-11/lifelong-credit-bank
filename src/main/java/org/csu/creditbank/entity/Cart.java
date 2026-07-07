package org.csu.creditbank.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("cart")
public class Cart {

    @TableId
    private Long id;
    private Long learnerId;
    private Long productId;
    private String productName;
    private Integer creditPrice;
    private Integer num;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
