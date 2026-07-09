package org.csu.creditbank.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("integrity_rating")
public class IntegrityRating extends BaseEntity {
    @TableId
    private Long id;
    private Long learnerId;
    private Integer score;
    private String levelName;
    private Integer learningScore;
    private Integer forumScore;
    private Integer signinScore;
    private Integer achievementScore;
    private Integer jobScore;
    private String remark;
    private Long institutionId;
}
