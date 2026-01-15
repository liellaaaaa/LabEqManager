package org.cong.backend.user.dto;

import lombok.Data;

@Data
public class RoleResponse {
    private Long id;
    private String name;
    private String code;
    private String description;
}

