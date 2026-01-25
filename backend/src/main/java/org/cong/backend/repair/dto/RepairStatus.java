package org.cong.backend.repair.dto;

/**
 * 与数据库 equipment_repair.status 约定保持一致
 * 0-待维修，1-维修中，2-已修好，3-无法修复
 */
public class RepairStatus {
    public static final int PENDING = 0;
    public static final int REPAIRING = 1;
    public static final int FIXED = 2;
    public static final int UNREPAIRABLE = 3;

    private RepairStatus() {}

    public static String toName(Integer status) {
        if (status == null) return "";
        return switch (status) {
            case PENDING -> "待维修";
            case REPAIRING -> "维修中";
            case FIXED -> "已修好";
            case UNREPAIRABLE -> "无法修复";
            default -> "未知";
        };
    }
}

