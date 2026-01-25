package org.cong.backend.scrap.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.cong.backend.common.ApiResponse;
import org.cong.backend.scrap.dto.*;
import org.cong.backend.scrap.service.EquipmentScrapService;
import org.cong.backend.user.dto.PageResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/scrap")
@SecurityRequirement(name = "bearerAuth")
public class EquipmentScrapController {

    private final EquipmentScrapService scrapService;

    public EquipmentScrapController(EquipmentScrapService scrapService) {
        this.scrapService = scrapService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('admin','teacher','student')")
    public ResponseEntity<ApiResponse<PageResponse<ScrapListResponse>>> getScrapList(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Long equipmentId,
            @RequestParam(required = false) Long applicantId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate applyDateStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate applyDateEnd,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder) {
        PageResponse<ScrapListResponse> result = scrapService.getScrapList(
                page, size, equipmentId, applicantId, status, applyDateStart, applyDateEnd, sortBy, sortOrder);
        return ResponseEntity.ok(ApiResponse.success("获取成功", result));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin','teacher','student')")
    public ResponseEntity<ApiResponse<ScrapDetailResponse>> getScrapDetail(@PathVariable Long id) {
        ScrapDetailResponse result = scrapService.getScrapDetail(id);
        return ResponseEntity.ok(ApiResponse.success("获取成功", result));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('admin','teacher','student')")
    public ResponseEntity<ApiResponse<ScrapDetailResponse>> createScrap(@Validated @RequestBody CreateScrapRequest request) {
        ScrapDetailResponse result = scrapService.createScrap(request);
        return ResponseEntity.ok(ApiResponse.success("报废申请提交成功", result));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<ApiResponse<ScrapDetailResponse>> approveScrap(
            @PathVariable Long id,
            @Validated @RequestBody ApproveScrapRequest request) {
        ScrapDetailResponse result = scrapService.approveScrap(id, request);
        return ResponseEntity.ok(ApiResponse.success("审批成功", result));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<ApiResponse<ScrapStatsResponse>> getScrapStats(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        ScrapStatsResponse result = scrapService.getScrapStats(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("获取成功", result));
    }
}

