package org.cong.backend.scrap.service;

import org.cong.backend.common.BusinessException;
import org.cong.backend.equipment.entity.Equipment;
import org.cong.backend.equipment.repository.EquipmentRepository;
import org.cong.backend.scrap.dto.*;
import org.cong.backend.scrap.entity.EquipmentScrap;
import org.cong.backend.scrap.repository.EquipmentScrapRepository;
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
public class EquipmentScrapService {

    private final EquipmentScrapRepository scrapRepository;
    private final EquipmentRepository equipmentRepository;
    private final UserRepository userRepository;

    public EquipmentScrapService(EquipmentScrapRepository scrapRepository,
                                EquipmentRepository equipmentRepository,
                                UserRepository userRepository) {
        this.scrapRepository = scrapRepository;
        this.equipmentRepository = equipmentRepository;
        this.userRepository = userRepository;
    }

    public PageResponse<ScrapListResponse> getScrapList(Integer page, Integer size,
                                                      Long equipmentId, Long applicantId, Integer status,
                                                      LocalDate applyDateStart, LocalDate applyDateEnd,
                                                      String sortBy, String sortOrder) {
        int pageNum = (page == null || page < 1) ? 1 : page;
        int pageSize = (size == null || size < 1) ? 10 : size;

        Sort sort = Sort.by("createTime").descending();
        if (StringUtils.hasText(sortBy)) {
            Sort.Direction dir = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
            sort = Sort.by(dir, sortBy);
        }
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, sort);

        Long effectiveApplicantId = resolveEffectiveApplicantIdForQuery(applicantId);

        Specification<EquipmentScrap> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (equipmentId != null) {
                predicates.add(cb.equal(root.get("equipmentId"), equipmentId));
            }
            if (effectiveApplicantId != null) {
                predicates.add(cb.equal(root.get("applicantId"), effectiveApplicantId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (applyDateStart != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("applyDate"), applyDateStart.atStartOfDay()));
            }
            if (applyDateEnd != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("applyDate"), applyDateEnd.atTime(23, 59, 59)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<EquipmentScrap> scrapPage = scrapRepository.findAll(spec, pageable);
        List<ScrapListResponse> list = scrapPage.getContent().stream().map(this::toScrapListResponse).toList();

        return new PageResponse<>(list, scrapPage.getTotalElements(), pageNum, pageSize);
    }

    public ScrapDetailResponse getScrapDetail(Long id) {
        EquipmentScrap scrap = scrapRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("报废记录不存在"));

        // 权限：管理员可看全部；教师/学生只能看自己的
        if (!SecurityUtils.isAdmin()) {
            Long currentUserId = resolveCurrentUserId();
            if (currentUserId == null || !currentUserId.equals(scrap.getApplicantId())) {
                throw new BusinessException(403, "无权限查看该报废记录");
            }
        }
        return toScrapDetailResponse(scrap);
    }

    @Transactional
    public ScrapDetailResponse createScrap(CreateScrapRequest request) {
        User currentUser = resolveCurrentUser();
        if (currentUser == null) {
            throw new BusinessException(401, "未授权访问，请先登录");
        }

        Equipment equipment = equipmentRepository.findById(request.getEquipmentId())
                .orElseThrow(() -> BusinessException.notFound("设备不存在"));

        EquipmentScrap scrap = new EquipmentScrap();
        scrap.setEquipmentId(request.getEquipmentId());
        scrap.setApplicantId(currentUser.getId());
        scrap.setApplyDate(request.getApplyDate());
        scrap.setScrapReason(request.getScrapReason());
        scrap.setStatus(ScrapStatus.PENDING_APPROVAL);

        EquipmentScrap saved = scrapRepository.save(scrap);
        return toScrapDetailResponse(saved);
    }

    @Transactional
    public ScrapDetailResponse approveScrap(Long id, ApproveScrapRequest request) {
        EquipmentScrap scrap = scrapRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("报废记录不存在"));
        if (scrap.getStatus() == null || scrap.getStatus() != ScrapStatus.PENDING_APPROVAL) {
            throw BusinessException.badRequest("该报废申请已审批过，无法重复审批");
        }
        if (request.getStatus() == null || (request.getStatus() != ScrapStatus.APPROVED && request.getStatus() != ScrapStatus.REJECTED)) {
            throw BusinessException.badRequest("参数错误：审批状态无效（必须是1-通过或2-拒绝）");
        }

        User approver = resolveCurrentUser();
        if (approver == null) {
            throw new BusinessException(401, "未授权访问，请先登录");
        }

        scrap.setStatus(request.getStatus());
        scrap.setApproverId(approver.getId());
        scrap.setApproveTime(LocalDateTime.now());
        scrap.setApproveRemark(StringUtils.hasText(request.getRemark()) ? request.getRemark() : null);

        EquipmentScrap saved = scrapRepository.save(scrap);
        return toScrapDetailResponse(saved);
    }

    public ScrapStatsResponse getScrapStats(LocalDate startDate, LocalDate endDate) {
        Specification<EquipmentScrap> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("applyDate"), startDate.atStartOfDay()));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("applyDate"), endDate.atTime(23, 59, 59)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<EquipmentScrap> scraps = scrapRepository.findAll(spec);
        int totalCount = scraps.size();
        int pendingCount = 0;
        int approvedCount = 0;
        int rejectedCount = 0;

        for (EquipmentScrap scrap : scraps) {
            Integer status = scrap.getStatus();
            if (status == null) continue;
            switch (status) {
                case ScrapStatus.PENDING_APPROVAL -> pendingCount++;
                case ScrapStatus.APPROVED -> approvedCount++;
                case ScrapStatus.REJECTED -> rejectedCount++;
            }
        }

        double approvalRate = totalCount > 0 ? (double) approvedCount / totalCount : 0.0;

        ScrapStatsResponse resp = new ScrapStatsResponse();
        resp.setTotalCount(totalCount);
        resp.setPendingCount(pendingCount);
        resp.setApprovedCount(approvedCount);
        resp.setRejectedCount(rejectedCount);
        resp.setApprovalRate(approvalRate);
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

    private Long resolveEffectiveApplicantIdForQuery(Long applicantIdParam) {
        if (SecurityUtils.isAdmin()) {
            return applicantIdParam;
        }
        // 非管理员强制只能看自己的
        return resolveCurrentUserId();
    }

    private ScrapListResponse toScrapListResponse(EquipmentScrap scrap) {
        ScrapListResponse resp = new ScrapListResponse();
        fillCommon(resp, scrap);
        return resp;
    }

    private ScrapDetailResponse toScrapDetailResponse(EquipmentScrap scrap) {
        ScrapDetailResponse resp = new ScrapDetailResponse();
        fillCommon(resp, scrap);
        return resp;
    }

    private void fillCommon(Object respObj, EquipmentScrap scrap) {
        Equipment equipment = equipmentRepository.findById(scrap.getEquipmentId()).orElse(null);
        User applicant = userRepository.findById(scrap.getApplicantId()).orElse(null);
        User approver = scrap.getApproverId() == null ? null : userRepository.findById(scrap.getApproverId()).orElse(null);

        if (respObj instanceof ScrapListResponse resp) {
            resp.setId(scrap.getId());
            resp.setEquipmentId(scrap.getEquipmentId());
            if (equipment != null) {
                resp.setEquipmentName(equipment.getName());
                resp.setEquipmentModel(equipment.getModel());
                resp.setEquipmentAssetCode(equipment.getAssetCode());
            }
            resp.setApplicantId(scrap.getApplicantId());
            if (applicant != null) {
                resp.setApplicantName(applicant.getName());
            }
            resp.setApplyDate(scrap.getApplyDate());
            resp.setScrapReason(scrap.getScrapReason());
            resp.setStatus(scrap.getStatus());
            resp.setStatusName(ScrapStatus.toName(scrap.getStatus()));
            resp.setApproverId(scrap.getApproverId());
            if (approver != null) {
                resp.setApproverName(approver.getName());
            }
            resp.setApproveTime(scrap.getApproveTime());
            resp.setApproveRemark(scrap.getApproveRemark());
            resp.setCreateTime(scrap.getCreateTime());
            resp.setUpdateTime(scrap.getUpdateTime());
        } else if (respObj instanceof ScrapDetailResponse resp) {
            resp.setId(scrap.getId());
            resp.setEquipmentId(scrap.getEquipmentId());
            if (equipment != null) {
                resp.setEquipmentName(equipment.getName());
                resp.setEquipmentModel(equipment.getModel());
                resp.setEquipmentAssetCode(equipment.getAssetCode());
            }
            resp.setApplicantId(scrap.getApplicantId());
            if (applicant != null) {
                resp.setApplicantName(applicant.getName());
            }
            resp.setApplyDate(scrap.getApplyDate());
            resp.setScrapReason(scrap.getScrapReason());
            resp.setStatus(scrap.getStatus());
            resp.setStatusName(ScrapStatus.toName(scrap.getStatus()));
            resp.setApproverId(scrap.getApproverId());
            if (approver != null) {
                resp.setApproverName(approver.getName());
            }
            resp.setApproveTime(scrap.getApproveTime());
            resp.setApproveRemark(scrap.getApproveRemark());
            resp.setCreateTime(scrap.getCreateTime());
            resp.setUpdateTime(scrap.getUpdateTime());
        }
    }
}

