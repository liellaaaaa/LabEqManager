package org.cong.backend.common;

/**
 * 业务异常：用于将“可预期的业务错误”按指定 HTTP 状态码返回给前端。
 */
public class BusinessException extends RuntimeException {

    private final int httpStatus;
    private final int code;

    public BusinessException(int httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.code = httpStatus;
    }

    public BusinessException(int httpStatus, int code, String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.code = code;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public int getCode() {
        return code;
    }

    public static BusinessException badRequest(String message) {
        return new BusinessException(400, message);
    }

    public static BusinessException notFound(String message) {
        return new BusinessException(404, message);
    }

    public static BusinessException conflict(String message) {
        return new BusinessException(409, message);
    }

    public static BusinessException unauthorized(String message) {
        return new BusinessException(401, message);
    }
}


