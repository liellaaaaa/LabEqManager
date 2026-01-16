package org.cong.backend.user.controller;

import org.cong.backend.common.ApiResponse;
import org.cong.backend.security.SecurityUtils;
import org.cong.backend.user.dto.*;
import org.cong.backend.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<ApiResponse<PageResponse<UserListResponse>>> getUserList(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String roleCode,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder) {
        PageResponse<UserListResponse> result = userService.getUserList(
                page, size, username, name, department, roleCode, status, sortBy, sortOrder);
        return ResponseEntity.ok(ApiResponse.success("获取成功", result));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('admin') or @permissionService.isCurrentUser(#id)")
    public ResponseEntity<ApiResponse<UserListResponse>> getUserById(
            @PathVariable Long id) {
        try {
            UserListResponse user = userService.getUserById(id);
            return ResponseEntity.ok(ApiResponse.success("获取成功", user));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error(404, e.getMessage()));
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<ApiResponse<UserListResponse>> createUser(
            @Validated @RequestBody CreateUserRequest request) {
        try {
            if (request.getUsername() == null || request.getPassword() == null ||
                request.getName() == null || request.getRoleCode() == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(400, "参数错误：用户名、密码、姓名和角色代码不能为空"));
            }
            UserListResponse user = userService.createUser(request);
            return ResponseEntity.ok(ApiResponse.success("创建成功", user));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("用户名已存在")) {
                return ResponseEntity.status(409)
                        .body(ApiResponse.error(409, e.getMessage()));
            }
            if (e.getMessage().contains("角色代码不存在")) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(400, e.getMessage()));
            }
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<ApiResponse<UserListResponse>> updateUser(
            @PathVariable Long id, @RequestBody UpdateUserRequest request) {
        try {
            UserListResponse user = userService.updateUser(id, request);
            return ResponseEntity.ok(ApiResponse.success("更新成功", user));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("用户不存在")) {
                return ResponseEntity.status(404)
                        .body(ApiResponse.error(404, e.getMessage()));
            }
            if (e.getMessage().contains("角色代码不存在")) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(400, e.getMessage()));
            }
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(ApiResponse.success("删除成功", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error(404, e.getMessage()));
        }
    }

    @PutMapping("/{id}/password")
    @PreAuthorize("hasRole('admin') or @permissionService.isCurrentUser(#id)")
    public ResponseEntity<ApiResponse<Void>> updatePassword(
            @PathVariable Long id,
            @RequestBody UpdatePasswordRequest request) {
        try {
            String currentUsername = SecurityUtils.getCurrentUsername();
            userService.updatePassword(id, request, currentUsername);
            return ResponseEntity.ok(ApiResponse.success("密码更新成功", null));
        } catch (RuntimeException e) {
            int statusCode = 400;
            if (e.getMessage().contains("用户不存在")) {
                statusCode = 404;
            } else if (e.getMessage().contains("旧密码错误") || 
                       e.getMessage().contains("未授权")) {
                statusCode = 401;
            }
            return ResponseEntity.status(statusCode)
                    .body(ApiResponse.error(statusCode, e.getMessage()));
        }
    }

    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<ApiResponse<Void>> batchDeleteUsers(
            @RequestBody BatchDeleteRequest request) {
        try {
            if (request.getIds() == null || request.getIds().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(400, "参数错误：用户ID列表不能为空"));
            }
            userService.batchDeleteUsers(request.getIds());
            return ResponseEntity.ok(ApiResponse.success("批量删除成功", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, e.getMessage()));
        }
    }
}

