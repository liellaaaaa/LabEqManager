package org.cong.backend.auth.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserInfoResponse {

    private Long id;
    private String username;
    private String name;
    private String email;
    private String phone;
    private String department;
    private String roleCode;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}


