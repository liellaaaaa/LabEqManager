package org.cong.backend.scrap.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApproveScrapRequest {
    @NotNull(message = "审批状态不能为空")
    private Integer status;

    private String remark;
}

