package org.cong.backend.auth;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.cong.backend.auth.dto.LoginRequest;
import org.cong.backend.auth.dto.LoginResponse;
import org.cong.backend.auth.dto.UserInfoResponse;
import org.cong.backend.common.ApiResponse;
import org.cong.backend.security.JwtTokenProvider;
import org.cong.backend.user.entity.User;
import org.cong.backend.user.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(JwtTokenProvider tokenProvider,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Validated @RequestBody LoginRequest request) {
        if (request.getUsername() == null || request.getPassword() == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "参数错误：用户名和密码不能为空"));
        }
        User user = userRepository.findByUsername(request.getUsername()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error(401, "用户名或密码错误"));
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error(401, "用户已被禁用"));
        }
        boolean passwordOk = false;
        // 优先按 BCrypt 校验（符合最终安全要求）
        try {
            passwordOk = passwordEncoder.matches(request.getPassword(), user.getPassword());
        } catch (Exception ignored) {
        }
        // 兼容开发阶段可能存在的明文密码（如直接将密码字段设置为 'admin123'）
        if (!passwordOk && request.getPassword().equals(user.getPassword())) {
            passwordOk = true;
        }
        if (!passwordOk) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error(401, "用户名或密码错误"));
        }
        Map<String, Object> claims = new HashMap<>();
        claims.put("roleCode", user.getRoleCode());
        String token = tokenProvider.generateToken(user.getUsername(), claims);

        LoginResponse resp = new LoginResponse();
        resp.setToken(token);
        resp.setUserInfo(toUserInfo(user));
        return ResponseEntity.ok(ApiResponse.success("登录成功", resp));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        // JWT是无状态的，服务端不需要维护会话，这里只是逻辑上的注销
        return ResponseEntity.ok(ApiResponse.success("注销成功", null));
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<UserInfoResponse>> me(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || authHeader.trim().isEmpty()) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error(401, "未授权访问，请先登录"));
        }
        
        try {
            // 兼容 "Bearer {token}" 和直接 "{token}" 两种格式
            String token = authHeader.trim();
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            String username = tokenProvider.getUsername(token);
            
            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) {
                return ResponseEntity.status(404)
                        .body(ApiResponse.error(404, "用户不存在"));
            }
            
            return ResponseEntity.ok(ApiResponse.success("获取成功", toUserInfo(user)));
        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error(401, "Token已过期或无效，请重新登录"));
        }
    }

    private UserInfoResponse toUserInfo(User user) {
        UserInfoResponse info = new UserInfoResponse();
        info.setId(user.getId());
        info.setUsername(user.getUsername());
        info.setName(user.getName());
        info.setEmail(user.getEmail());
        info.setPhone(user.getPhone());
        info.setDepartment(user.getDepartment());
        info.setRoleCode(user.getRoleCode());
        info.setStatus(user.getStatus());
        info.setCreateTime(user.getCreateTime());
        info.setUpdateTime(user.getUpdateTime());
        return info;
    }
}


