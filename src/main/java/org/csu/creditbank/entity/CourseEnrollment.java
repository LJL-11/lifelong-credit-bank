package org.csu.creditbank.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("course_enrollment")
public class CourseEnrollment extends BaseEntity {
    @TableId
    private Long id;
    private Long learnerId;
    private Long courseId;
    private String enrollStatus;
    private String reviewer;
    private LocalDateTime reviewedAt;
    private String remark;
    private Long institutionId;
}
