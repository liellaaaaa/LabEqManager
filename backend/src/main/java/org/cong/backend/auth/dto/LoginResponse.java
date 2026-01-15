package org.cong.backend.auth.dto;

import lombok.Data;

@Data
public class LoginResponse {

    private String token;
    private UserInfoResponse userInfo;
}


