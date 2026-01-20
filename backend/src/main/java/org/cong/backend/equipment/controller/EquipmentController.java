package org.cong.backend.equipment.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.cong.backend.common.BusinessException;
import org.cong.backend.common.ApiResponse;
import org.cong.backend.equipment.dto.*;
import org.cong.backend.equipment.service.EquipmentService;
import org.cong.backend.equipment.service.EquipmentStatusService;
import org.cong.backend.user.dto.PageResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/equipment")
@SecurityRequirement(name = "bearerAuth")
public class EquipmentController {

    private final EquipmentService equipmentService;
    private final EquipmentStatusService equipmentStatusService;

    public EquipmentController(EquipmentService equipmentService,
                              EquipmentStatusService equipmentStatusService) {
        this.equipmentService = equipmentService;
        this.equipmentStatusService = equipmentStatusService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('admin', 'teacher', 'student')")
    public ResponseEntity<ApiResponse<PageResponse<EquipmentListResponse>>> getEquipmentList(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) String specification,
            @RequestParam(required = false) String assetCode,
            @RequestParam(required = false) String supplier,
            @RequestParam(required = false) String statusCode,
            @RequestParam(required = false) Long laboratoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate purchaseDateStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate purchaseDateEnd,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder) {
        PageResponse<EquipmentListResponse> result = equipmentService.getEquipmentList(
                page, size, name, model, specification, assetCode, supplier, statusCode,
                laboratoryId, purchaseDateStart, purchaseDateEnd, sortBy, sortOrder);
        return ResponseEntity.ok(ApiResponse.success("获取成功", result));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin', 'teacher', 'student')")
    public ResponseEntity<ApiResponse<EquipmentDetailResponse>> getEquipmentById(
            @PathVariable Long id) {
        try {
            EquipmentDetailResponse equipment = equipmentService.getEquipmentById(id);
            return ResponseEntity.ok(ApiResponse.success("获取成功", equipment));
        } catch (BusinessException e) {
            return ResponseEntity.status(e.getHttpStatus())
                    .body(ApiResponse.error(e.getCode(), e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error(404, e.getMessage()));
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<ApiResponse<EquipmentListResponse>> createEquipment(
            @Validated @RequestBody CreateEquipmentRequest request) {
        try {
            EquipmentListResponse equipment = equipmentService.createEquipment(request);
            return ResponseEntity.ok(ApiResponse.success("创建成功", equipment));
        } catch (BusinessException e) {
            return ResponseEntity.status(e.getHttpStatus())
                    .body(ApiResponse.error(e.getCode(), e.getMessage()));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("资产编号已存在")) {
                return ResponseEntity.status(409)
                        .body(ApiResponse.error(409, e.getMessage()));
            }
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<ApiResponse<EquipmentListResponse>> updateEquipment(
            @PathVariable Long id, @RequestBody UpdateEquipmentRequest request) {
        try {
            EquipmentListResponse equipment = equipmentService.updateEquipment(id, request);
            return ResponseEntity.ok(ApiResponse.success("更新成功", equipment));
        } catch (BusinessException e) {
            return ResponseEntity.status(e.getHttpStatus())
                    .body(ApiResponse.error(e.getCode(), e.getMessage()));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("设备不存在")) {
                return ResponseEntity.status(404)
                        .body(ApiResponse.error(404, e.getMessage()));
            }
            if (e.getMessage().contains("资产编号已存在")) {
                return ResponseEntity.status(409)
                        .body(ApiResponse.error(409, e.getMessage()));
            }
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<ApiResponse<Void>> deleteEquipment(@PathVariable Long id) {
        try {
            equipmentService.deleteEquipment(id);
            return ResponseEntity.ok(ApiResponse.success("删除成功", null));
        } catch (BusinessException e) {
            return ResponseEntity.status(e.getHttpStatus())
                    .body(ApiResponse.error(e.getCode(), e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error(404, e.getMessage()));
        }
    }

    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<ApiResponse<Void>> batchDeleteEquipment(
            @RequestBody org.cong.backend.user.dto.BatchDeleteRequest request) {
        try {
            if (request.getIds() == null || request.getIds().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(400, "参数错误：设备ID列表不能为空"));
            }
            equipmentService.batchDeleteEquipment(request.getIds());
            return ResponseEntity.ok(ApiResponse.success("批量删除成功", null));
        } catch (BusinessException e) {
            return ResponseEntity.status(e.getHttpStatus())
                    .body(ApiResponse.error(e.getCode(), e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('admin', 'teacher')")
    public ResponseEntity<ApiResponse<EquipmentListResponse>> updateStatus(
            @PathVariable Long id, @RequestBody UpdateEquipmentStatusRequest request) {
        try {
            EquipmentListResponse equipment = equipmentService.updateStatus(id, request);
            return ResponseEntity.ok(ApiResponse.success("状态更新成功", equipment));
        } catch (BusinessException e) {
            return ResponseEntity.status(e.getHttpStatus())
                    .body(ApiResponse.error(e.getCode(), e.getMessage()));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("设备不存在") || e.getMessage().contains("设备状态不存在")) {
                return ResponseEntity.status(404)
                        .body(ApiResponse.error(404, e.getMessage()));
            }
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, e.getMessage()));
        }
    }

    @GetMapping("/status")
    @PreAuthorize("hasAnyRole('admin', 'teacher', 'student')")
    public ResponseEntity<ApiResponse<List<EquipmentStatusResponse>>> getEquipmentStatusList() {
        List<EquipmentStatusResponse> result = equipmentStatusService.getAllStatuses();
        return ResponseEntity.ok(ApiResponse.success("获取成功", result));
    }
}

