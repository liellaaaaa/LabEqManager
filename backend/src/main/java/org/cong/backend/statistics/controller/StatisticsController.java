package org.cong.backend.statistics.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.cong.backend.common.ApiResponse;
import org.cong.backend.statistics.dto.BorrowStatsResponse;
import org.cong.backend.statistics.dto.EquipmentUsageStatsResponse;
import org.cong.backend.statistics.dto.ReminderResponse;
import org.cong.backend.statistics.service.StatisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/statistics")
@SecurityRequirement(name = "bearerAuth")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    /**
     * 获取设备使用次数统计
     */
    @GetMapping("/equipment-usage")
    @PreAuthorize("hasAnyRole('admin','teacher')")
    public ResponseEntity<ApiResponse<EquipmentUsageStatsResponse>> getEquipmentUsageStats() {
        EquipmentUsageStatsResponse result = statisticsService.getEquipmentUsageStats();
        return ResponseEntity.ok(ApiResponse.success("获取成功", result));
    }

    /**
     * 获取借用/逾期统计
     */
    @GetMapping("/borrow-stats")
    @PreAuthorize("hasAnyRole('admin','teacher')")
    public ResponseEntity<ApiResponse<BorrowStatsResponse>> getBorrowStats() {
        BorrowStatsResponse result = statisticsService.getBorrowStats();
        return ResponseEntity.ok(ApiResponse.success("获取成功", result));
    }

    /**
     * 获取到期提醒
     * 返回当前用户即将到期的借用和预约记录
     */
    @GetMapping("/reminders")
    @PreAuthorize("hasAnyRole('admin','teacher','student')")
    public ResponseEntity<ApiResponse<ReminderResponse>> getReminders() {
        ReminderResponse result = statisticsService.getReminders();
        return ResponseEntity.ok(ApiResponse.success("获取成功", result));
    }
}

