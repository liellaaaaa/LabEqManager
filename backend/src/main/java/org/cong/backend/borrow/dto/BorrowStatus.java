package org.cong.backend.borrow.dto;

/**
 * 与数据库 equipment_borrow.status 约定保持一致
 * 0-待审批，1-已通过，2-已拒绝，3-已借出，4-已归还，5-已逾期
 */
public class BorrowStatus {
    public static final int PENDING_APPROVAL = 0;
    public static final int APPROVED = 1;
    public static final int REJECTED = 2;
    public static final int BORROWED = 3;
    public static final int RETURNED = 4;
    public static final int OVERDUE = 5;

    private BorrowStatus() {}

    public static String toName(Integer status) {
        if (status == null) return "";
        return switch (status) {
            case PENDING_APPROVAL -> "待审批";
            case APPROVED -> "已通过";
            case REJECTED -> "已拒绝";
            case BORROWED -> "已借出";
            case RETURNED -> "已归还";
            case OVERDUE -> "已逾期";
            default -> "未知";
        };
    }
}


