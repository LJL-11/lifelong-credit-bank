package org.csu.creditbank.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("job_application")
public class JobApplication extends BaseEntity {
    @TableId
    private Long id;
    private Long jobId;
    private Long learnerId;
    private String resumeSummary;
    private String resumeUrl;
    private String resumeFileName;
    private String applyStatus;
    private String reviewer;
    private LocalDateTime reviewedAt;
    private String remark;
    private Long institutionId;

    @TableField(exist = false)
    private String learnerName;
}
