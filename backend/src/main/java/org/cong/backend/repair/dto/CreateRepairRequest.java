package org.cong.backend.repair.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CreateRepairRequest {
    @NotNull(message = "设备ID不能为空")
    private Long equipmentId;

    @NotNull(message = "报修日期不能为空")
    private LocalDateTime reportDate;

    @NotNull(message = "故障描述不能为空")
    private String faultDescription;
}

