package org.cong.backend.borrow.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.cong.backend.borrow.dto.*;
import org.cong.backend.borrow.service.EquipmentBorrowService;
import org.cong.backend.common.ApiResponse;
import org.cong.backend.user.dto.PageResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/borrow")
@SecurityRequirement(name = "bearerAuth")
public class EquipmentBorrowController {

    private final EquipmentBorrowService borrowService;

    public EquipmentBorrowController(EquipmentBorrowService borrowService) {
        this.borrowService = borrowService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('admin','teacher','student')")
    public ResponseEntity<ApiResponse<PageResponse<BorrowListResponse>>> getBorrowList(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Long equipmentId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate borrowDateStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate borrowDateEnd,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder) {
        PageResponse<BorrowListResponse> result = borrowService.getBorrowList(
                page, size, equipmentId, userId, status, borrowDateStart, borrowDateEnd, sortBy, sortOrder);
        return ResponseEntity.ok(ApiResponse.success("获取成功", result));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin','teacher','student')")
    public ResponseEntity<ApiResponse<BorrowDetailResponse>> getBorrowDetail(@PathVariable Long id) {
        BorrowDetailResponse result = borrowService.getBorrowDetail(id);
        return ResponseEntity.ok(ApiResponse.success("获取成功", result));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('admin','teacher','student')")
    public ResponseEntity<ApiResponse<BorrowDetailResponse>> createBorrow(@Validated @RequestBody CreateBorrowRequest request) {
        BorrowDetailResponse result = borrowService.createBorrow(request);
        return ResponseEntity.ok(ApiResponse.success("借用申请提交成功", result));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<ApiResponse<BorrowDetailResponse>> approveBorrow(
            @PathVariable Long id,
            @Validated @RequestBody ApproveBorrowRequest request) {
        BorrowDetailResponse result = borrowService.approveBorrow(id, request);
        return ResponseEntity.ok(ApiResponse.success("审批成功", result));
    }

    @PutMapping("/{id}/borrow")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<ApiResponse<BorrowDetailResponse>> confirmBorrow(
            @PathVariable Long id,
            @RequestBody(required = false) ConfirmBorrowRequest request) {
        BorrowDetailResponse result = borrowService.confirmBorrow(id, request);
        return ResponseEntity.ok(ApiResponse.success("设备借出确认成功", result));
    }

    @PutMapping("/{id}/return")
    @PreAuthorize("hasRole('admin') or hasAnyRole('teacher','student')")
    public ResponseEntity<ApiResponse<BorrowDetailResponse>> returnBorrow(
            @PathVariable Long id,
            @RequestBody(required = false) ReturnBorrowRequest request) {
        BorrowDetailResponse result = borrowService.returnBorrow(id, request);
        return ResponseEntity.ok(ApiResponse.success("设备归还成功", result));
    }

    @PutMapping("/mark-overdue")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<ApiResponse<MarkOverdueResponse>> markOverdue() {
        MarkOverdueResponse result = borrowService.markOverdue();
        return ResponseEntity.ok(ApiResponse.success("逾期标记完成", result));
    }

    @GetMapping("/available-quantity/{equipmentId}")
    @PreAuthorize("hasAnyRole('admin','teacher','student')")
    public ResponseEntity<ApiResponse<AvailableQuantityResponse>> getAvailableQuantity(@PathVariable Long equipmentId) {
        AvailableQuantityResponse result = borrowService.getAvailableQuantity(equipmentId);
        return ResponseEntity.ok(ApiResponse.success("获取成功", result));
    }
}


