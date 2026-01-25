package org.cong.backend.scrap.dto;

/**
 * 与数据库 equipment_scrap.status 约定保持一致
 * 0-待审批，1-已通过，2-已拒绝
 */
public class ScrapStatus {
    public static final int PENDING_APPROVAL = 0;
    public static final int APPROVED = 1;
    public static final int REJECTED = 2;

    private ScrapStatus() {}

    public static String toName(Integer status) {
        if (status == null) return "";
        return switch (status) {
            case PENDING_APPROVAL -> "待审批";
            case APPROVED -> "已通过";
            case REJECTED -> "已拒绝";
            default -> "未知";
        };
    }
}

