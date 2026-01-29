package org.cong.backend.laboratory.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateLaboratoryRequest {
    @NotBlank(message = "实验室名称不能为空")
    private String name;

    @NotBlank(message = "实验室编号不能为空")
    private String code;

    @NotBlank(message = "实验室位置不能为空")
    private String location;
    private Integer capacity;
    private String type;
    private Integer status = 1; // 默认可用
    private Long managerId;
    private String description;
}

