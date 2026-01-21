package org.cong.backend.borrow.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReturnBorrowRequest {
    /**
     * 实际归还时间（可选，不传则使用当前时间）
     */
    private LocalDateTime actualReturnDate;

    /**
     * 归还备注（表结构里没有单独字段，这里会合并写入 approveRemark）
     */
    private String remark;
}


