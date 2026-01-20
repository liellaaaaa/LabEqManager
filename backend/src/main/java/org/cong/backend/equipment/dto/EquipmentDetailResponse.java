package org.cong.backend.equipment.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class EquipmentDetailResponse {
    private Long id;
    private String name;
    private String model;
    private String specification;
    private String assetCode;
    private Double unitPrice;
    private Integer quantity;
    private String supplier;
    private LocalDate purchaseDate;
    private Integer warrantyPeriod;
    private Long statusId;
    private String statusName;
    private String statusCode;
    private Long laboratoryId;
    private String laboratoryName;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

