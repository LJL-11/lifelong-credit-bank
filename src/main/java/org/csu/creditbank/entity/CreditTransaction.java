package org.csu.creditbank.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("credit_transaction")
public class CreditTransaction extends BaseEntity {

    @TableId
    private Long id;
    private Long learnerId;
    private Long accountId;
    private String transactionType;
    private Integer amount;
    private Integer balanceBefore;
    private Integer balanceAfter;
    private String sourceType;
    private String sourceNo;
    private String remark;
    private Long relatedTransactionId;
    private String conversionId;
    private Integer feeAmount;
}
