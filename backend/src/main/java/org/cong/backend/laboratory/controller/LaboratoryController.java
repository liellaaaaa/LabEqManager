package org.cong.backend.laboratory.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.cong.backend.common.ApiResponse;
import org.cong.backend.laboratory.dto.*;
import org.cong.backend.laboratory.service.LaboratoryService;
import org.cong.backend.user.dto.PageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/laboratory")
@SecurityRequirement(name = "bearerAuth")
public class LaboratoryController {

    private final LaboratoryService laboratoryService;
    private final org.cong.backend.equipment.service.EquipmentService equipmentService;

    public LaboratoryController(LaboratoryService laboratoryService,
                               org.cong.backend.equipment.service.EquipmentService equipmentService) {
        this.laboratoryService = laboratoryService;
        this.equipmentService = equipmentService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('admin', 'teacher', 'student')")
    public ResponseEntity<ApiResponse<PageResponse<LaboratoryListResponse>>> getLaboratoryList(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long managerId,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder) {
        PageResponse<LaboratoryListResponse> result = laboratoryService.getLaboratoryList(
                page, size, name, code, location, type, status, managerId, sortBy, sortOrder);
        return ResponseEntity.ok(ApiResponse.success("获取成功", result));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin', 'teacher', 'student')")
    public ResponseEntity<ApiResponse<LaboratoryDetailResponse>> getLaboratoryById(
            @PathVariable Long id) {
        try {
            LaboratoryDetailResponse laboratory = laboratoryService.getLaboratoryById(id);
            return ResponseEntity.ok(ApiResponse.success("获取成功", laboratory));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error(404, e.getMessage()));
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<ApiResponse<LaboratoryListResponse>> createLaboratory(
            @Validated @RequestBody CreateLaboratoryRequest request) {
        try {
            LaboratoryListResponse laboratory = laboratoryService.createLaboratory(request);
            return ResponseEntity.ok(ApiResponse.success("创建成功", laboratory));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("实验室编号已存在")) {
                return ResponseEntity.status(409)
                        .body(ApiResponse.error(409, e.getMessage()));
            }
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<ApiResponse<LaboratoryListResponse>> updateLaboratory(
            @PathVariable Long id, @RequestBody UpdateLaboratoryRequest request) {
        try {
            LaboratoryListResponse laboratory = laboratoryService.updateLaboratory(id, request);
            return ResponseEntity.ok(ApiResponse.success("更新成功", laboratory));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("实验室不存在")) {
                return ResponseEntity.status(404)
                        .body(ApiResponse.error(404, e.getMessage()));
            }
            if (e.getMessage().contains("实验室编号已存在")) {
                return ResponseEntity.status(409)
                        .body(ApiResponse.error(409, e.getMessage()));
            }
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<ApiResponse<Void>> deleteLaboratory(@PathVariable Long id) {
        try {
            laboratoryService.deleteLaboratory(id);
            return ResponseEntity.ok(ApiResponse.success("删除成功", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error(404, e.getMessage()));
        }
    }

    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<ApiResponse<Void>> batchDeleteLaboratories(
            @RequestBody org.cong.backend.user.dto.BatchDeleteRequest request) {
        try {
            if (request.getIds() == null || request.getIds().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(400, "参数错误：实验室ID列表不能为空"));
            }
            laboratoryService.batchDeleteLaboratories(request.getIds());
            return ResponseEntity.ok(ApiResponse.success("批量删除成功", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<ApiResponse<LaboratoryListResponse>> updateStatus(
            @PathVariable Long id, @RequestBody UpdateStatusRequest request) {
        try {
            LaboratoryListResponse laboratory = laboratoryService.updateStatus(id, request);
            return ResponseEntity.ok(ApiResponse.success("状态更新成功", laboratory));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("实验室不存在")) {
                return ResponseEntity.status(404)
                        .body(ApiResponse.error(404, e.getMessage()));
            }
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, e.getMessage()));
        }
    }

    @GetMapping("/{id}/equipment")
    @PreAuthorize("hasAnyRole('admin', 'teacher')")
    public ResponseEntity<ApiResponse<PageResponse<org.cong.backend.equipment.dto.EquipmentListResponse>>> getLaboratoryEquipment(
            @PathVariable Long id,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String statusCode) {
        try {
            PageResponse<org.cong.backend.equipment.dto.EquipmentListResponse> result = 
                    equipmentService.getEquipmentList(page, size, null, null, null, null, null, 
                            statusCode, id, null, null, null, null);
            return ResponseEntity.ok(ApiResponse.success("获取成功", result));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error(404, e.getMessage()));
        }
    }
}

