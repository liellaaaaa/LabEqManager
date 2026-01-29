package org.cong.backend.laboratory.service;

import org.cong.backend.equipment.repository.EquipmentRepository;
import org.cong.backend.laboratory.dto.*;
import org.cong.backend.laboratory.entity.Laboratory;
import org.cong.backend.laboratory.repository.LaboratoryRepository;
import org.cong.backend.user.dto.PageResponse;
import org.cong.backend.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LaboratoryService {

    private final LaboratoryRepository laboratoryRepository;
    private final UserRepository userRepository;
    private final EquipmentRepository equipmentRepository;

    public LaboratoryService(LaboratoryRepository laboratoryRepository, 
                            UserRepository userRepository,
                            EquipmentRepository equipmentRepository) {
        this.laboratoryRepository = laboratoryRepository;
        this.userRepository = userRepository;
        this.equipmentRepository = equipmentRepository;
    }

    public PageResponse<LaboratoryListResponse> getLaboratoryList(Integer page, Integer size,
                                                                  String name, String code,
                                                                  String location, String type,
                                                                  Integer status, Long managerId,
                                                                  String sortBy, String sortOrder) {
        // 分页参数
        int pageNum = (page == null || page < 1) ? 1 : page;
        int pageSize = (size == null || size < 1) ? 10 : size;
        
        // 排序参数
        String sortField = StringUtils.hasText(sortBy) ? sortBy : "createTime";
        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortField);
        
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, sort);
        
        // 构建查询条件
        Specification<Laboratory> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (StringUtils.hasText(name)) {
                predicates.add(cb.like(root.get("name"), "%" + name + "%"));
            }
            if (StringUtils.hasText(code)) {
                predicates.add(cb.like(root.get("code"), "%" + code + "%"));
            }
            if (StringUtils.hasText(location)) {
                predicates.add(cb.like(root.get("location"), "%" + location + "%"));
            }
            if (StringUtils.hasText(type)) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (managerId != null) {
                predicates.add(cb.equal(root.get("managerId"), managerId));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        Page<Laboratory> laboratoryPage = laboratoryRepository.findAll(spec, pageable);
        
        List<LaboratoryListResponse> list = laboratoryPage.getContent().stream()
                .map(this::toLaboratoryListResponse)
                .collect(Collectors.toList());
        
        return new PageResponse<>(list, laboratoryPage.getTotalElements(), pageNum, pageSize);
    }

    public LaboratoryDetailResponse getLaboratoryById(Long id) {
        Laboratory laboratory = laboratoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("实验室不存在"));
        
        LaboratoryDetailResponse response = toLaboratoryDetailResponse(laboratory);
        
        // 统计设备数量
        long equipmentCount = equipmentRepository.findAll().stream()
                .filter(eq -> eq.getLaboratoryId().equals(id))
                .count();
        response.setEquipmentCount((int) equipmentCount);
        
        return response;
    }

    @Transactional
    public LaboratoryListResponse createLaboratory(CreateLaboratoryRequest request) {
        // 检查实验室编号是否已存在
        if (laboratoryRepository.findByCode(request.getCode()).isPresent()) {
            throw new RuntimeException("实验室编号已存在");
        }
        
        // 验证负责人是否存在
        if (request.getManagerId() != null) {
            if (!userRepository.findById(request.getManagerId()).isPresent()) {
                throw new RuntimeException("负责人不存在");
            }
        }
        
        Laboratory laboratory = new Laboratory();
        laboratory.setName(request.getName());
        laboratory.setCode(request.getCode());
        laboratory.setLocation(request.getLocation());
        laboratory.setCapacity(request.getCapacity());
        laboratory.setType(request.getType());
        laboratory.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        laboratory.setManagerId(request.getManagerId());
        laboratory.setDescription(request.getDescription());
        
        Laboratory saved = laboratoryRepository.save(laboratory);
        return toLaboratoryListResponse(saved);
    }

    @Transactional
    public LaboratoryListResponse updateLaboratory(Long id, UpdateLaboratoryRequest request) {
        Laboratory laboratory = laboratoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("实验室不存在"));
        
        if (StringUtils.hasText(request.getName())) {
            laboratory.setName(request.getName());
        }
        if (StringUtils.hasText(request.getCode())) {
            // 检查编号是否已被其他实验室使用
            laboratoryRepository.findByCode(request.getCode())
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(id)) {
                            throw new RuntimeException("实验室编号已存在");
                        }
                    });
            laboratory.setCode(request.getCode());
        }
        if (StringUtils.hasText(request.getLocation())) {
            laboratory.setLocation(request.getLocation());
        }
        if (request.getCapacity() != null) {
            laboratory.setCapacity(request.getCapacity());
        }
        if (StringUtils.hasText(request.getType())) {
            laboratory.setType(request.getType());
        }
        if (request.getManagerId() != null) {
            if (!userRepository.findById(request.getManagerId()).isPresent()) {
                throw new RuntimeException("负责人不存在");
            }
            laboratory.setManagerId(request.getManagerId());
        }
        if (request.getDescription() != null) {
            laboratory.setDescription(request.getDescription());
        }
        
        Laboratory updated = laboratoryRepository.save(laboratory);
        return toLaboratoryListResponse(updated);
    }

    @Transactional
    public void deleteLaboratory(Long id) {
        if (!laboratoryRepository.existsById(id)) {
            throw new RuntimeException("实验室不存在");
        }
        laboratoryRepository.deleteById(id);
    }

    @Transactional
    public void batchDeleteLaboratories(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new RuntimeException("实验室ID列表不能为空");
        }
        laboratoryRepository.deleteAllById(ids);
    }

    @Transactional
    public LaboratoryListResponse updateStatus(Long id, UpdateStatusRequest request) {
        Laboratory laboratory = laboratoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("实验室不存在"));
        
        if (request.getStatus() == null || (request.getStatus() < 0 || request.getStatus() > 2)) {
            throw new RuntimeException("状态值无效，必须是0、1或2");
        }
        
        laboratory.setStatus(request.getStatus());
        Laboratory updated = laboratoryRepository.save(laboratory);
        return toLaboratoryListResponse(updated);
    }

    private LaboratoryListResponse toLaboratoryListResponse(Laboratory laboratory) {
        LaboratoryListResponse response = new LaboratoryListResponse();
        response.setId(laboratory.getId());
        response.setName(laboratory.getName());
        response.setCode(laboratory.getCode());
        response.setLocation(laboratory.getLocation());
        response.setCapacity(laboratory.getCapacity());
        response.setType(laboratory.getType());
        response.setStatus(laboratory.getStatus());
        response.setManagerId(laboratory.getManagerId());
        response.setDescription(laboratory.getDescription());
        response.setCreateTime(laboratory.getCreateTime());
        response.setUpdateTime(laboratory.getUpdateTime());
        
        // 获取负责人姓名
        if (laboratory.getManagerId() != null) {
            userRepository.findById(laboratory.getManagerId())
                    .ifPresent(user -> response.setManagerName(user.getName()));
        }
        
        return response;
    }

    private LaboratoryDetailResponse toLaboratoryDetailResponse(Laboratory laboratory) {
        LaboratoryDetailResponse response = new LaboratoryDetailResponse();
        response.setId(laboratory.getId());
        response.setName(laboratory.getName());
        response.setCode(laboratory.getCode());
        response.setLocation(laboratory.getLocation());
        response.setCapacity(laboratory.getCapacity());
        response.setType(laboratory.getType());
        response.setStatus(laboratory.getStatus());
        response.setManagerId(laboratory.getManagerId());
        response.setDescription(laboratory.getDescription());
        response.setCreateTime(laboratory.getCreateTime());
        response.setUpdateTime(laboratory.getUpdateTime());
        
        // 获取负责人姓名
        if (laboratory.getManagerId() != null) {
            userRepository.findById(laboratory.getManagerId())
                    .ifPresent(user -> response.setManagerName(user.getName()));
        }
        
        return response;
    }
}

