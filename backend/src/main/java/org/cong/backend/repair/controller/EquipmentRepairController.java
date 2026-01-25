package org.cong.backend.repair.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.cong.backend.common.ApiResponse;
import org.cong.backend.repair.dto.*;
import org.cong.backend.repair.service.EquipmentRepairService;
import org.cong.backend.user.dto.PageResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/repair")
@SecurityRequirement(name = "bearerAuth")
public class EquipmentRepairController {

    private final EquipmentRepairService repairService;

    public EquipmentRepairController(EquipmentRepairService repairService) {
        this.repairService = repairService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('admin','teacher','student')")
    public ResponseEntity<ApiResponse<PageResponse<RepairListResponse>>> getRepairList(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Long equipmentId,
            @RequestParam(required = false) Long reporterId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate reportDateStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate reportDateEnd,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder) {
        PageResponse<RepairListResponse> result = repairService.getRepairList(
                page, size, equipmentId, reporterId, status, reportDateStart, reportDateEnd, sortBy, sortOrder);
        return ResponseEntity.ok(ApiResponse.success("获取成功", result));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin','teacher','student')")
    public ResponseEntity<ApiResponse<RepairDetailResponse>> getRepairDetail(@PathVariable Long id) {
        RepairDetailResponse result = repairService.getRepairDetail(id);
        return ResponseEntity.ok(ApiResponse.success("获取成功", result));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('admin','teacher','student')")
    public ResponseEntity<ApiResponse<RepairDetailResponse>> createRepair(@Validated @RequestBody CreateRepairRequest request) {
        RepairDetailResponse result = repairService.createRepair(request);
        return ResponseEntity.ok(ApiResponse.success("维修申请提交成功", result));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<ApiResponse<RepairDetailResponse>> updateRepairStatus(
            @PathVariable Long id,
            @Validated @RequestBody UpdateRepairStatusRequest request) {
        RepairDetailResponse result = repairService.updateRepairStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("维修记录状态更新成功", result));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<ApiResponse<RepairStatsResponse>> getRepairStats(
            @RequestParam(required = false) Long equipmentId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        RepairStatsResponse result = repairService.getRepairStats(equipmentId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("获取成功", result));
    }
}

