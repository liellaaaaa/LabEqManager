package org.cong.backend.equipment.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateEquipmentRequest {
    private String name;
    private String model;
    private String specification;
    private String assetCode;
    private Double unitPrice;
    private Integer quantity;
    private String supplier;
    private LocalDate purchaseDate;
    private Integer warrantyPeriod;
    private Long laboratoryId;
    private String description;
}

