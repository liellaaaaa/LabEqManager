package org.cong.backend.reservation.dto;

/**
 * 与数据库 laboratory_reservation.status 约定保持一致
 * 0-待审批，1-已通过，2-已拒绝，3-已取消，4-已完成，5-已使用
 */
public class ReservationStatus {
    public static final int PENDING_APPROVAL = 0;
    public static final int APPROVED = 1;
    public static final int REJECTED = 2;
    public static final int CANCELLED = 3;
    public static final int COMPLETED = 4;
    public static final int IN_USE = 5;

    private ReservationStatus() {}

    public static String toName(Integer status) {
        if (status == null) return "";
        return switch (status) {
            case PENDING_APPROVAL -> "待审批";
            case APPROVED -> "已通过";
            case REJECTED -> "已拒绝";
            case CANCELLED -> "已取消";
            case COMPLETED -> "已完成";
            case IN_USE -> "已使用";
            default -> "未知";
        };
    }
}

