package org.cong.backend.laboratory.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LaboratoryListResponse {
    private Long id;
    private String name;
    private String code;
    private String location;
    private Integer capacity;
    private String type;
    private Integer status;
    private Long managerId;
    private String managerName;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

