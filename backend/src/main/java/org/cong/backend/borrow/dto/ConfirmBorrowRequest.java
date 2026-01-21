package org.cong.backend.borrow.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConfirmBorrowRequest {
    /**
     * 实际借出时间（可选，不传则使用当前时间）
     */
    private LocalDateTime borrowDate;
}


