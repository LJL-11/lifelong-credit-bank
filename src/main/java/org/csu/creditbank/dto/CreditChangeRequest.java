package org.csu.creditbank.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreditChangeRequest {

    @NotNull
    private Long learnerId;

    @NotNull
    @Min(1)
    private Integer amount;

    private String sourceType;
    private String sourceNo;
    private String remark;
}
