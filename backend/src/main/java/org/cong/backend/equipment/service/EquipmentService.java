package org.cong.backend.equipment.service;

import org.cong.backend.equipment.dto.*;
import org.cong.backend.equipment.entity.Equipment;

import org.cong.backend.equipment.repository.EquipmentRepository;
import org.cong.backend.equipment.repository.EquipmentStatusRepository;

import org.cong.backend.laboratory.repository.LaboratoryRepository;
import org.cong.backend.user.dto.PageResponse;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final EquipmentStatusRepository equipmentStatusRepository;
    private final LaboratoryRepository laboratoryRepository;

    public EquipmentService(EquipmentRepository equipmentRepository,
                           EquipmentStatusRepository equipmentStatusRepository,
                           LaboratoryRepository laboratoryRepository) {
        this.equipmentRepository = equipmentRepository;
        this.equipmentStatusRepository = equipmentStatusRepository;
        this.laboratoryRepository = laboratoryRepository;
    }

    public PageResponse<EquipmentListResponse> getEquipmentList(Integer page, Integer size,
                                                               String name, String model,
                                                               String specification, String assetCode,
                                                               String supplier, String statusCode,
                                                               Long laboratoryId, LocalDate purchaseDateStart,
                                                               LocalDate purchaseDateEnd, String sortBy, String sortOrder) {
        // 分页参数
        int pageNum = (page == null || page < 1) ? 1 : page;
        int pageSize = (size == null || size < 1) ? 10 : size;
        
        // 排序参数未使用，因为使用手动分页方式，实际排序在数据库查询时未应用
        
        // Pageable 参数已注释，使用手动分页
        
        // 构建查询条件
        Specification<Equipment> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (StringUtils.hasText(name)) {
                predicates.add(cb.like(root.get("name"), "%" + name + "%"));
            }
            if (StringUtils.hasText(model)) {
                predicates.add(cb.like(root.get("model"), "%" + model + "%"));
            }
            if (StringUtils.hasText(specification)) {
                predicates.add(cb.like(root.get("specification"), "%" + specification + "%"));
            }
            if (StringUtils.hasText(assetCode)) {
                predicates.add(cb.equal(root.get("assetCode"), assetCode));
            }
            if (StringUtils.hasText(supplier)) {
                predicates.add(cb.like(root.get("supplier"), "%" + supplier + "%"));
            }
            // 状态代码过滤在查询后处理
            if (laboratoryId != null) {
                predicates.add(cb.equal(root.get("laboratoryId"), laboratoryId));
            }
            if (purchaseDateStart != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("purchaseDate"), purchaseDateStart));
            }
            if (purchaseDateEnd != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("purchaseDate"), purchaseDateEnd));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        // 先查询所有符合条件的设备
        Page<Equipment> equipmentPage = equipmentRepository.findAll(spec, Pageable.unpaged());
        
        // 转换为响应对象并过滤状态代码
        List<EquipmentListResponse> allList = equipmentPage.getContent().stream()
                .map(this::toEquipmentListResponse)
                .filter(response -> {
                    if (StringUtils.hasText(statusCode)) {
                        return statusCode.equals(response.getStatusCode());
                    }
                    return true;
                })
                .collect(Collectors.toList());
        
        // 手动分页
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, allList.size());
        List<EquipmentListResponse> list = start < allList.size() 
                ? allList.subList(start, end) 
                : List.of();
        
        return new PageResponse<>(list, (long) allList.size(), pageNum, pageSize);
    }

    public EquipmentDetailResponse getEquipmentById(Long id) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("设备不存在"));
        return toEquipmentDetailResponse(equipment);
    }

    @Transactional
    public EquipmentListResponse createEquipment(CreateEquipmentRequest request) {
        // 检查资产编号是否已存在
        if (StringUtils.hasText(request.getAssetCode())) {
            if (equipmentRepository.findByAssetCode(request.getAssetCode()).isPresent()) {
                throw new RuntimeException("资产编号已存在");
            }
        }
        
        // 验证状态ID是否存在
        equipmentStatusRepository.findById(request.getStatusId())
                .orElseThrow(() -> new RuntimeException("设备状态不存在"));
        
        // 验证实验室ID是否存在
        laboratoryRepository.findById(request.getLaboratoryId())
                .orElseThrow(() -> new RuntimeException("实验室不存在"));
        
        Equipment equipment = new Equipment();
        equipment.setName(request.getName());
        equipment.setModel(request.getModel());
        equipment.setSpecification(request.getSpecification());
        equipment.setAssetCode(request.getAssetCode());
        equipment.setUnitPrice(request.getUnitPrice());
        equipment.setQuantity(request.getQuantity() != null ? request.getQuantity() : 1);
        equipment.setSupplier(request.getSupplier());
        equipment.setPurchaseDate(request.getPurchaseDate());
        equipment.setWarrantyPeriod(request.getWarrantyPeriod());
        equipment.setStatusId(request.getStatusId());
        equipment.setLaboratoryId(request.getLaboratoryId());
        equipment.setDescription(request.getDescription());
        
        Equipment saved = equipmentRepository.save(equipment);
        return toEquipmentListResponse(saved);
    }

    @Transactional
    public EquipmentListResponse updateEquipment(Long id, UpdateEquipmentRequest request) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("设备不存在"));
        
        if (StringUtils.hasText(request.getName())) {
            equipment.setName(request.getName());
        }
        if (StringUtils.hasText(request.getModel())) {
            equipment.setModel(request.getModel());
        }
        if (request.getSpecification() != null) {
            equipment.setSpecification(request.getSpecification());
        }
        if (StringUtils.hasText(request.getAssetCode())) {
            // 检查资产编号是否已被其他设备使用
            equipmentRepository.findByAssetCode(request.getAssetCode())
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(id)) {
                            throw new RuntimeException("资产编号已存在");
                        }
                    });
            equipment.setAssetCode(request.getAssetCode());
        }
        if (request.getUnitPrice() != null) {
            equipment.setUnitPrice(request.getUnitPrice());
        }
        if (request.getQuantity() != null) {
            equipment.setQuantity(request.getQuantity());
        }
        if (request.getSupplier() != null) {
            equipment.setSupplier(request.getSupplier());
        }
        if (request.getPurchaseDate() != null) {
            equipment.setPurchaseDate(request.getPurchaseDate());
        }
        if (request.getWarrantyPeriod() != null) {
            equipment.setWarrantyPeriod(request.getWarrantyPeriod());
        }
        if (request.getLaboratoryId() != null) {
            if (!laboratoryRepository.findById(request.getLaboratoryId()).isPresent()) {
                throw new RuntimeException("实验室不存在");
            }
            equipment.setLaboratoryId(request.getLaboratoryId());
        }
        if (request.getDescription() != null) {
            equipment.setDescription(request.getDescription());
        }
        
        Equipment updated = equipmentRepository.save(equipment);
        return toEquipmentListResponse(updated);
    }

    @Transactional
    public void deleteEquipment(Long id) {
        if (!equipmentRepository.existsById(id)) {
            throw new RuntimeException("设备不存在");
        }
        equipmentRepository.deleteById(id);
    }

    @Transactional
    public void batchDeleteEquipment(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new RuntimeException("设备ID列表不能为空");
        }
        equipmentRepository.deleteAllById(ids);
    }

    @Transactional
    public EquipmentListResponse updateStatus(Long id, UpdateEquipmentStatusRequest request) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("设备不存在"));
        
        equipmentStatusRepository.findById(request.getStatusId())
                .orElseThrow(() -> new RuntimeException("设备状态不存在"));
        
        equipment.setStatusId(request.getStatusId());
        Equipment updated = equipmentRepository.save(equipment);
        return toEquipmentListResponse(updated);
    }

    private EquipmentListResponse toEquipmentListResponse(Equipment equipment) {
        EquipmentListResponse response = new EquipmentListResponse();
        response.setId(equipment.getId());
        response.setName(equipment.getName());
        response.setModel(equipment.getModel());
        response.setSpecification(equipment.getSpecification());
        response.setAssetCode(equipment.getAssetCode());
        response.setUnitPrice(equipment.getUnitPrice());
        response.setQuantity(equipment.getQuantity());
        response.setSupplier(equipment.getSupplier());
        response.setPurchaseDate(equipment.getPurchaseDate());
        response.setWarrantyPeriod(equipment.getWarrantyPeriod());
        response.setStatusId(equipment.getStatusId());
        response.setLaboratoryId(equipment.getLaboratoryId());
        response.setDescription(equipment.getDescription());
        response.setCreateTime(equipment.getCreateTime());
        response.setUpdateTime(equipment.getUpdateTime());
        
        // 获取状态信息
        equipmentStatusRepository.findById(equipment.getStatusId())
                .ifPresent(status -> {
                    response.setStatusName(status.getName());
                    response.setStatusCode(status.getCode());
                });
        
        // 获取实验室信息
        laboratoryRepository.findById(equipment.getLaboratoryId())
                .ifPresent(lab -> response.setLaboratoryName(lab.getName()));
        
        return response;
    }

    private EquipmentDetailResponse toEquipmentDetailResponse(Equipment equipment) {
        EquipmentDetailResponse response = new EquipmentDetailResponse();
        response.setId(equipment.getId());
        response.setName(equipment.getName());
        response.setModel(equipment.getModel());
        response.setSpecification(equipment.getSpecification());
        response.setAssetCode(equipment.getAssetCode());
        response.setUnitPrice(equipment.getUnitPrice());
        response.setQuantity(equipment.getQuantity());
        response.setSupplier(equipment.getSupplier());
        response.setPurchaseDate(equipment.getPurchaseDate());
        response.setWarrantyPeriod(equipment.getWarrantyPeriod());
        response.setStatusId(equipment.getStatusId());
        response.setLaboratoryId(equipment.getLaboratoryId());
        response.setDescription(equipment.getDescription());
        response.setCreateTime(equipment.getCreateTime());
        response.setUpdateTime(equipment.getUpdateTime());
        
        // 获取状态信息
        equipmentStatusRepository.findById(equipment.getStatusId())
                .ifPresent(status -> {
                    response.setStatusName(status.getName());
                    response.setStatusCode(status.getCode());
                });
        
        // 获取实验室信息
        laboratoryRepository.findById(equipment.getLaboratoryId())
                .ifPresent(lab -> response.setLaboratoryName(lab.getName()));
        
        return response;
    }
}

