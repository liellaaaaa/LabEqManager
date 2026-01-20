package org.cong.backend.equipment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateEquipmentRequest {
    @NotBlank(message = "设备名称不能为空")
    private String name;

    @NotBlank(message = "设备型号不能为空")
    private String model;

    private String specification;
    private String assetCode;

    @NotNull(message = "单价不能为空")
    private Double unitPrice;

    private Integer quantity = 1;
    private String supplier;

    @NotNull(message = "购置日期不能为空")
    private LocalDate purchaseDate;

    private Integer warrantyPeriod;

    @NotNull(message = "设备状态ID不能为空")
    private Long statusId;

    @NotNull(message = "所属实验室ID不能为空")
    private Long laboratoryId;

    private String description;
}

