package org.cong.backend.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 处理 Spring Security 权限异常和其他通用异常
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理访问被拒绝异常（403）
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(403, "无权限访问该资源"));
    }

    /**
     * 处理认证凭证未找到异常（401）
     */
    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationCredentialsNotFoundException(
            AuthenticationCredentialsNotFoundException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(401, "未授权访问，请先登录"));
    }

    /**
     * 处理业务异常（400/404/409 等）
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        return ResponseEntity.status(e.getHttpStatus())
                .body(ApiResponse.error(e.getCode(), e.getMessage()));
    }
}

