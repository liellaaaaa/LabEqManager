package org.cong.backend.user.dto;

import lombok.Data;

@Data
public class UpdatePasswordRequest {
    private String oldPassword; // 当前用户修改自己密码时必填
    private String newPassword;
}

