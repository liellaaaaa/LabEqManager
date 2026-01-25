package org.cong.backend.repair.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UpdateRepairStatusRequest {
    @NotNull(message = "维修状态不能为空")
    private Integer status;

    private String repairResult;

    private LocalDateTime repairDate;
}

