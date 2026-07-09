package org.csu.creditbank.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("achievement")
public class Achievement extends BaseEntity {

    @TableId
    private Long id;
    private Long learnerId;
    private String achievementName;
    private String achievementType;
    private Integer suggestedCredits;
    private Long institutionId;
    private String auditStatus;
    private String auditor;
    private LocalDateTime auditedAt;
    private String proofUrl;
    private String rejectReason;
}
