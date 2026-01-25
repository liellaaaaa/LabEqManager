package org.cong.backend.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 到期提醒响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReminderResponse {
    
    /**
     * 是否有提醒（用于前端显示红点）
     */
    private Boolean hasReminder;
    
    /**
     * 提醒总数
     */
    private Integer reminderCount;
    
    /**
     * 即将到期的借用记录
     */
    private List<BorrowReminderItem> borrowReminders;
    
    /**
     * 即将到期的预约记录
     */
    private List<ReservationReminderItem> reservationReminders;
    
    /**
     * 借用提醒项
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BorrowReminderItem {
        /**
         * 借用记录ID
         */
        private Long id;
        
        /**
         * 设备名称
         */
        private String equipmentName;
        
        /**
         * 计划归还日期
         */
        private LocalDateTime planReturnDate;
        
        /**
         * 剩余天数（负数表示已逾期）
         */
        private Long remainingDays;
        
        /**
         * 是否已逾期
         */
        private Boolean isOverdue;
    }
    
    /**
     * 预约提醒项
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReservationReminderItem {
        /**
         * 预约记录ID
         */
        private Long id;
        
        /**
         * 实验室名称
         */
        private String laboratoryName;
        
        /**
         * 预约日期
         */
        private LocalDate reserveDate;
        
        /**
         * 开始时间
         */
        private LocalTime startTime;
        
        /**
         * 结束时间
         */
        private LocalTime endTime;
        
        /**
         * 剩余天数（负数表示已过期）
         */
        private Long remainingDays;
        
        /**
         * 是否已过期
         */
        private Boolean isExpired;
    }
}

