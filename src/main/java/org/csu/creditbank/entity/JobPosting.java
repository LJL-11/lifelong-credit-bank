package org.csu.creditbank.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("job_posting")
public class JobPosting extends BaseEntity {

    @TableId
    private Long id;
    private String companyName;
    private String positionName;
    private String city;
    private String requirement;
    private String contact;
    private String status;
}
