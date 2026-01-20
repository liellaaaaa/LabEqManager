package org.cong.backend.laboratory.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LaboratoryDetailResponse {
    private Long id;
    private String name;
    private String code;
    private String location;
    private Double area;
    private Integer capacity;
    private String type;
    private Integer status;
    private Long managerId;
    private String managerName;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer equipmentCount;
}

