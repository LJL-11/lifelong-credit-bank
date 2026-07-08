package org.csu.creditbank.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("forum_post_like")
public class ForumPostLike {

    @TableId
    private Long id;
    private Long postId;
    private Long learnerId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
