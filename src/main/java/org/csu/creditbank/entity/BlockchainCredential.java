package org.csu.creditbank.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("blockchain_credential")
public class BlockchainCredential extends BaseEntity {

    @TableId
    private Long id;
    private Long learnerId;
    private String credentialType;
    private String businessNo;
    private String hashValue;
    private String chainStatus;
}
