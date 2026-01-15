package org.cong.backend.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateUserRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "姓名不能为空")
    private String name;

    private String email;
    private String phone;
    private String department;

    @NotBlank(message = "角色代码不能为空")
    private String roleCode;

    private Integer status = 1; // 默认启用
}

