package org.cong.backend.repair.service;

import org.cong.backend.common.BusinessException;
import org.cong.backend.equipment.entity.Equipment;
import org.cong.backend.equipment.repository.EquipmentRepository;
import org.cong.backend.repair.dto.*;
import org.cong.backend.repair.entity.EquipmentRepair;
import org.cong.backend.repair.repository.EquipmentRepairRepository;
import org.cong.backend.security.SecurityUtils;
import org.cong.backend.user.dto.PageResponse;
import org.cong.backend.user.entity.User;
import org.cong.backend.user.repository.UserRepository;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class EquipmentRepairService {

    private final EquipmentRepairRepository repairRepository;
    private final EquipmentRepository equipmentRepository;
    private final UserRepository userRepository;

    public EquipmentRepairService(EquipmentRepairRepository repairRepository,
                                 EquipmentRepository equipmentRepository,
                                 UserRepository userRepository) {
        this.repairRepository = repairRepository;
        this.equipmentRepository = equipmentRepository;
        this.userRepository = userRepository;
    }

    public PageResponse<RepairListResponse> getRepairList(Integer page, Integer size,
                                                         Long equipmentId, Long reporterId, Integer status,
                                                         LocalDate reportDateStart, LocalDate reportDateEnd,
                                                         String sortBy, String sortOrder) {
        int pageNum = (page == null || page < 1) ? 1 : page;
        int pageSize = (size == null || size < 1) ? 10 : size;

        Sort sort = Sort.by("createTime").descending();
        if (StringUtils.hasText(sortBy)) {
            Sort.Direction dir = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
            sort = Sort.by(dir, sortBy);
        }
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, sort);

        Long effectiveReporterId = resolveEffectiveReporterIdForQuery(reporterId);

        Specification<EquipmentRepair> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (equipmentId != null) {
                predicates.add(cb.equal(root.get("equipmentId"), equipmentId));
            }
            if (effectiveReporterId != null) {
                predicates.add(cb.equal(root.get("reporterId"), effectiveReporterId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (reportDateStart != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("reportDate"), reportDateStart.atStartOfDay()));
            }
            if (reportDateEnd != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("reportDate"), reportDateEnd.atTime(23, 59, 59)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<EquipmentRepair> repairPage = repairRepository.findAll(spec, pageable);
        List<RepairListResponse> list = repairPage.getContent().stream().map(this::toRepairListResponse).toList();

        return new PageResponse<>(list, repairPage.getTotalElements(), pageNum, pageSize);
    }

    public RepairDetailResponse getRepairDetail(Long id) {
        EquipmentRepair repair = repairRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("维修记录不存在"));

        // 权限：管理员可看全部；教师/学生只能看自己的
        if (!SecurityUtils.isAdmin()) {
            Long currentUserId = resolveCurrentUserId();
            if (currentUserId == null || !currentUserId.equals(repair.getReporterId())) {
                throw new BusinessException(403, "无权限查看该维修记录");
            }
        }
        return toRepairDetailResponse(repair);
    }

    @Transactional
    public RepairDetailResponse createRepair(CreateRepairRequest request) {
        User currentUser = resolveCurrentUser();
        if (currentUser == null) {
            throw new BusinessException(401, "未授权访问，请先登录");
        }

        Equipment equipment = equipmentRepository.findById(request.getEquipmentId())
                .orElseThrow(() -> BusinessException.notFound("设备不存在"));

        EquipmentRepair repair = new EquipmentRepair();
        repair.setEquipmentId(request.getEquipmentId());
        repair.setReporterId(currentUser.getId());
        repair.setReportDate(request.getReportDate());
        repair.setFaultDescription(request.getFaultDescription());
        repair.setStatus(RepairStatus.PENDING);

        EquipmentRepair saved = repairRepository.save(repair);
        return toRepairDetailResponse(saved);
    }

    @Transactional
    public RepairDetailResponse updateRepairStatus(Long id, UpdateRepairStatusRequest request) {
        EquipmentRepair repair = repairRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("维修记录不存在"));

        if (request.getStatus() == null || request.getStatus() < 0 || request.getStatus() > 3) {
            throw BusinessException.badRequest("参数错误：维修状态无效（必须是0-3之间的整数）");
        }

        // 当状态为2（已修好）或3（无法修复）时，维修结果和维修日期必填
        if ((request.getStatus() == RepairStatus.FIXED || request.getStatus() == RepairStatus.UNREPAIRABLE)) {
            if (!StringUtils.hasText(request.getRepairResult())) {
                throw BusinessException.badRequest("参数错误：当状态为2或3时，维修结果不能为空");
            }
            if (request.getRepairDate() == null) {
                throw BusinessException.badRequest("参数错误：当状态为2或3时，维修日期不能为空");
            }
        }

        repair.setStatus(request.getStatus());
        if (StringUtils.hasText(request.getRepairResult())) {
            repair.setRepairResult(request.getRepairResult());
        }
        if (request.getRepairDate() != null) {
            repair.setRepairDate(request.getRepairDate());
        }

        EquipmentRepair saved = repairRepository.save(repair);
        return toRepairDetailResponse(saved);
    }

    public RepairStatsResponse getRepairStats(Long equipmentId, LocalDate startDate, LocalDate endDate) {
        Specification<EquipmentRepair> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (equipmentId != null) {
                predicates.add(cb.equal(root.get("equipmentId"), equipmentId));
            }
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("reportDate"), startDate.atStartOfDay()));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("reportDate"), endDate.atTime(23, 59, 59)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<EquipmentRepair> repairs = repairRepository.findAll(spec);
        int totalCount = repairs.size();
        int pendingCount = 0;
        int repairingCount = 0;
        int fixedCount = 0;
        int unrepairableCount = 0;

        for (EquipmentRepair repair : repairs) {
            Integer status = repair.getStatus();
            if (status == null) continue;
            switch (status) {
                case RepairStatus.PENDING -> pendingCount++;
                case RepairStatus.REPAIRING -> repairingCount++;
                case RepairStatus.FIXED -> fixedCount++;
                case RepairStatus.UNREPAIRABLE -> unrepairableCount++;
            }
        }

        double repairRate = totalCount > 0 ? (double) fixedCount / totalCount : 0.0;

        RepairStatsResponse resp = new RepairStatsResponse();
        resp.setTotalCount(totalCount);
        resp.setPendingCount(pendingCount);
        resp.setRepairingCount(repairingCount);
        resp.setFixedCount(fixedCount);
        resp.setUnrepairableCount(unrepairableCount);
        resp.setRepairRate(repairRate);
        return resp;
    }

    private User resolveCurrentUser() {
        String username = SecurityUtils.getCurrentUsername();
        if (!StringUtils.hasText(username)) return null;
        return userRepository.findByUsername(username).orElse(null);
    }

    private Long resolveCurrentUserId() {
        User u = resolveCurrentUser();
        return u == null ? null : u.getId();
    }

    private Long resolveEffectiveReporterIdForQuery(Long reporterIdParam) {
        if (SecurityUtils.isAdmin()) {
            return reporterIdParam;
        }
        // 非管理员强制只能看自己的
        return resolveCurrentUserId();
    }

    private RepairListResponse toRepairListResponse(EquipmentRepair repair) {
        RepairListResponse resp = new RepairListResponse();
        fillCommon(resp, repair);
        return resp;
    }

    private RepairDetailResponse toRepairDetailResponse(EquipmentRepair repair) {
        RepairDetailResponse resp = new RepairDetailResponse();
        fillCommon(resp, repair);
        return resp;
    }

    private void fillCommon(Object respObj, EquipmentRepair repair) {
        Equipment equipment = equipmentRepository.findById(repair.getEquipmentId()).orElse(null);
        User reporter = userRepository.findById(repair.getReporterId()).orElse(null);

        if (respObj instanceof RepairListResponse resp) {
            resp.setId(repair.getId());
            resp.setEquipmentId(repair.getEquipmentId());
            if (equipment != null) {
                resp.setEquipmentName(equipment.getName());
                resp.setEquipmentModel(equipment.getModel());
                resp.setEquipmentAssetCode(equipment.getAssetCode());
            }
            resp.setReporterId(repair.getReporterId());
            if (reporter != null) {
                resp.setReporterName(reporter.getName());
            }
            resp.setReportDate(repair.getReportDate());
            resp.setFaultDescription(repair.getFaultDescription());
            resp.setRepairResult(repair.getRepairResult());
            resp.setRepairDate(repair.getRepairDate());
            resp.setStatus(repair.getStatus());
            resp.setStatusName(RepairStatus.toName(repair.getStatus()));
            resp.setCreateTime(repair.getCreateTime());
            resp.setUpdateTime(repair.getUpdateTime());
        } else if (respObj instanceof RepairDetailResponse resp) {
            resp.setId(repair.getId());
            resp.setEquipmentId(repair.getEquipmentId());
            if (equipment != null) {
                resp.setEquipmentName(equipment.getName());
                resp.setEquipmentModel(equipment.getModel());
                resp.setEquipmentAssetCode(equipment.getAssetCode());
            }
            resp.setReporterId(repair.getReporterId());
            if (reporter != null) {
                resp.setReporterName(reporter.getName());
            }
            resp.setReportDate(repair.getReportDate());
            resp.setFaultDescription(repair.getFaultDescription());
            resp.setRepairResult(repair.getRepairResult());
            resp.setRepairDate(repair.getRepairDate());
            resp.setStatus(repair.getStatus());
            resp.setStatusName(RepairStatus.toName(repair.getStatus()));
            resp.setCreateTime(repair.getCreateTime());
            resp.setUpdateTime(repair.getUpdateTime());
        }
    }
}

