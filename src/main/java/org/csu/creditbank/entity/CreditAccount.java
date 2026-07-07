package org.csu.creditbank.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("credit_account")
public class CreditAccount extends BaseEntity {

    @TableId
    private Long id;
    private Long learnerId;
    private Integer totalCredits;
    private Integer availableCredits;
    private Integer frozenCredits;
    private String accountStatus;
}
