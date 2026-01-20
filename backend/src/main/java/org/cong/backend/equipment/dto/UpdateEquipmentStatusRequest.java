package org.cong.backend.equipment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateEquipmentStatusRequest {
    @NotNull(message = "状态ID不能为空")
    private Long statusId;
}

