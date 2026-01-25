package org.cong.backend.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 借用/逾期统计响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowStatsResponse {
    
    /**
     * 总借用记录数
     */
    private Long totalBorrowCount;
    
    /**
     * 待审批数量
     */
    private Long pendingCount;
    
    /**
     * 已通过数量
     */
    private Long approvedCount;
    
    /**
     * 已借出数量
     */
    private Long borrowedCount;
    
    /**
     * 已归还数量
     */
    private Long returnedCount;
    
    /**
     * 已逾期数量
     */
    private Long overdueCount;
    
    /**
     * 已拒绝数量
     */
    private Long rejectedCount;
}

