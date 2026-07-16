package org.csu.creditbank.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sign_in_record")
public class SignInRecord extends BaseEntity {

    @TableId
    private Long id;
    private Long learnerId;
    private LocalDate signDate;
    private Integer rewardCredits;
    private String signType;
    private Long institutionId;

    @TableField(exist = false)
    private String learnerName;
}
