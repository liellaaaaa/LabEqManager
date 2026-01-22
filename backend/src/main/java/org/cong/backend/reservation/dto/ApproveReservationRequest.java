package org.cong.backend.reservation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApproveReservationRequest {

    @NotNull(message = "审批状态不能为空")
    private Integer status;

    private String remark;
}

