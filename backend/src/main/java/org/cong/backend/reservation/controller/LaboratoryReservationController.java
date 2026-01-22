package org.cong.backend.reservation.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.cong.backend.common.ApiResponse;
import org.cong.backend.reservation.dto.*;
import org.cong.backend.reservation.service.LaboratoryReservationService;
import org.cong.backend.user.dto.PageResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/reservation")
@SecurityRequirement(name = "bearerAuth")
public class LaboratoryReservationController {

    private final LaboratoryReservationService reservationService;

    public LaboratoryReservationController(LaboratoryReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('admin','teacher','student')")
    public ResponseEntity<ApiResponse<PageResponse<ReservationListResponse>>> getReservationList(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Long laboratoryId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate reserveDate,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder) {
        PageResponse<ReservationListResponse> result = reservationService.getReservationList(
                page, size, laboratoryId, userId, reserveDate, status, sortBy, sortOrder);
        return ResponseEntity.ok(ApiResponse.success("获取成功", result));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin','teacher','student')")
    public ResponseEntity<ApiResponse<ReservationDetailResponse>> getReservationDetail(@PathVariable Long id) {
        ReservationDetailResponse result = reservationService.getReservationDetail(id);
        return ResponseEntity.ok(ApiResponse.success("获取成功", result));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('admin','teacher','student')")
    public ResponseEntity<ApiResponse<ReservationDetailResponse>> createReservation(
            @Validated @RequestBody CreateReservationRequest request) {
        ReservationDetailResponse result = reservationService.createReservation(request);
        return ResponseEntity.ok(ApiResponse.success("预约申请提交成功", result));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('admin','teacher','student')")
    public ResponseEntity<ApiResponse<ReservationDetailResponse>> cancelReservation(
            @PathVariable Long id,
            @RequestBody(required = false) CancelReservationRequest request) {
        if (request == null) {
            request = new CancelReservationRequest();
        }
        ReservationDetailResponse result = reservationService.cancelReservation(id, request);
        return ResponseEntity.ok(ApiResponse.success("预约取消成功", result));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<ApiResponse<ReservationDetailResponse>> approveReservation(
            @PathVariable Long id,
            @Validated @RequestBody ApproveReservationRequest request) {
        ReservationDetailResponse result = reservationService.approveReservation(id, request);
        return ResponseEntity.ok(ApiResponse.success("审批成功", result));
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<ApiResponse<ReservationDetailResponse>> completeReservation(
            @PathVariable Long id,
            @RequestBody(required = false) CompleteReservationRequest request) {
        if (request == null) {
            request = new CompleteReservationRequest();
        }
        ReservationDetailResponse result = reservationService.completeReservation(id, request);
        return ResponseEntity.ok(ApiResponse.success("预约已标记为完成", result));
    }

    @PostMapping("/check-conflict")
    @PreAuthorize("hasAnyRole('admin','teacher','student')")
    public ResponseEntity<ApiResponse<CheckConflictResponse>> checkConflict(
            @Validated @RequestBody CheckConflictRequest request) {
        CheckConflictResponse result = reservationService.checkConflict(request);
        return ResponseEntity.ok(ApiResponse.success("检查成功", result));
    }

    @GetMapping("/available-time")
    @PreAuthorize("hasAnyRole('admin','teacher','student')")
    public ResponseEntity<ApiResponse<AvailableTimeResponse>> getAvailableTime(
            @RequestParam Long laboratoryId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate reserveDate) {
        AvailableTimeResponse result = reservationService.getAvailableTime(laboratoryId, reserveDate);
        return ResponseEntity.ok(ApiResponse.success("获取成功", result));
    }
}

