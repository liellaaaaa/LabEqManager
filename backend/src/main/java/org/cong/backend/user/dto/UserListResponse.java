package org.cong.backend.user.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserListResponse {
    private Long id;
    private String username;
    private String name;
    private String email;
    private String phone;
    private String department;
    private String roleCode;
    private Integer status;
    private LocalDateTime createTime;
}

