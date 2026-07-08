package org.csu.creditbank.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("institution")
public class Institution extends BaseEntity {
    @TableId
    private Long id;
    private String name;
    private String code;
    private String contact;
    private String phone;
    private String status;
}
