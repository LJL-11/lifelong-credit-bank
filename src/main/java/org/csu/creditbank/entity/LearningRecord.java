package org.csu.creditbank.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("learning_record")
public class LearningRecord extends BaseEntity {

    @TableId
    private Long id;
    private Long learnerId;
    private Long courseId;
    private BigDecimal progress;
    private BigDecimal score;
    private String result;
    private LocalDateTime completedAt;
    private Long institutionId;

    @TableField(exist = false)
    private String learnerName;
}
