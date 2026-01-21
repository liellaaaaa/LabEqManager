package org.cong.backend.borrow.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApproveBorrowRequest {

    /**
     * 1-通过，2-拒绝
     */
    @NotNull(message = "审批状态不能为空")
    private Integer status;

    private String remark;
}


