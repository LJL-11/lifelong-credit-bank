package org.csu.creditbank.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("learner")
public class Learner extends BaseEntity {

    @TableId
    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private String educationLevel;
    private String status;

    @JsonIgnore
    private String password;

    private String role;
    private Long institutionId;
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String institutionName;
}
