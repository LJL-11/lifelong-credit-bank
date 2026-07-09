package org.csu.creditbank.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("forum_reply")
public class ForumReply extends BaseEntity {
    @TableId
    private Long id;
    private Long postId;
    private Long learnerId;
    private String content;
    private String status;
    private Long institutionId;

    @TableField(exist = false)
    private String learnerName;
}
