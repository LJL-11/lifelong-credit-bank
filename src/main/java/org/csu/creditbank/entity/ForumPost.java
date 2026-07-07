package org.csu.creditbank.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("forum_post")
public class ForumPost extends BaseEntity {

    @TableId
    private Long id;
    private Long learnerId;
    private String title;
    private String content;
    private String section;
    private Integer replyCount;
    private String status;

    /** 发帖人姓名（非数据库字段） */
    @TableField(exist = false)
    private String learnerName;
}
