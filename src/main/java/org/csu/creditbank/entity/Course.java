package org.csu.creditbank.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("course")
public class Course extends BaseEntity {

    @TableId
    private Long id;
    private String courseCode;
    private String courseName;
    private String provider;
    private String category;
    private BigDecimal creditValue;
    private Integer creditPoint;
    private String status;
    private Long institutionId;
    private String resourceTitle;
    private String resourceUrl;
    private String resourceSummary;
    private Integer estimatedMinutes;
    private String learningStages;

    @TableField(exist = false)
    private String enrollStatus;
}
