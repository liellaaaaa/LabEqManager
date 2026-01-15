package org.cong.backend.user.dto;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String name;
    private String email;
    private String phone;
    private String department;
    private String roleCode;
    private Integer status;
}

