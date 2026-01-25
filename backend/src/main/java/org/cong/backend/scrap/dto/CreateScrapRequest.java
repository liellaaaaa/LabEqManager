package org.cong.backend.scrap.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CreateScrapRequest {
    @NotNull(message = "设备ID不能为空")
    private Long equipmentId;

    @NotNull(message = "申请日期不能为空")
    private LocalDateTime applyDate;

    @NotNull(message = "报废原因不能为空")
    private String scrapReason;
}

