package org.cong.backend.laboratory.dto;

import lombok.Data;

@Data
public class UpdateLaboratoryRequest {
    private String name;
    private String code;
    private String location;
    private Double area;
    private Integer capacity;
    private String type;
    private Long managerId;
    private String description;
}

