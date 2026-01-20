package org.cong.backend.equipment.dto;

import lombok.Data;

@Data
public class EquipmentStatusResponse {
    private Long id;
    private String name;
    private String code;
    private String description;
}

