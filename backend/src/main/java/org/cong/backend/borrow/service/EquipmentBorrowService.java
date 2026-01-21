package org.cong.backend.borrow.service;

import org.cong.backend.borrow.dto.*;
import org.cong.backend.borrow.entity.EquipmentBorrow;
import org.cong.backend.borrow.repository.EquipmentBorrowRepository;
import org.cong.backend.common.BusinessException;
import org.cong.backend.equipment.entity.Equipment;
import org.cong.backend.equipment.entity.EquipmentStatus;
import org.cong.backend.equipment.repository.EquipmentRepository;
import org.cong.backend.equipment.repository.EquipmentStatusRepository;
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
public class EquipmentBorrowService {

    private final EquipmentBorrowRepository borrowRepository;
    private final EquipmentRepository equipmentRepository;
    private final EquipmentStatusRepository equipmentStatusRepository;
    private final UserRepository userRepository;

    public EquipmentBorrowService(EquipmentBorrowRepository borrowRepository,
                                 EquipmentRepository equipmentRepository,
                                 EquipmentStatusRepository equipmentStatusRepository,
                                 UserRepository userRepository) {
        this.borrowRepository = borrowRepository;
        this.equipmentRepository = equipmentRepository;
        this.equipmentStatusRepository = equipmentStatusRepository;
        this.userRepository = userRepository;
    }

    public PageResponse<BorrowListResponse> getBorrowList(Integer page, Integer size,
                                                         Long equipmentId, Long userId, Integer status,
                                                         LocalDate borrowDateStart, LocalDate borrowDateEnd,
                                                         String sortBy, String sortOrder) {
        int pageNum = (page == null || page < 1) ? 1 : page;
        int pageSize = (size == null || size < 1) ? 10 : size;

        Sort sort = Sort.by("createTime").descending();
        if (StringUtils.hasText(sortBy)) {
            Sort.Direction dir = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
            sort = Sort.by(dir, sortBy);
        }
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, sort);

        Long effectiveUserId = resolveEffectiveUserIdForQuery(userId);

        Specification<EquipmentBorrow> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (equipmentId != null) {
                predicates.add(cb.equal(root.get("equipmentId"), equipmentId));
            }
            if (effectiveUserId != null) {
                predicates.add(cb.equal(root.get("userId"), effectiveUserId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (borrowDateStart != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("borrowDate"), borrowDateStart.atStartOfDay()));
            }
            if (borrowDateEnd != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("borrowDate"), borrowDateEnd.atTime(23, 59, 59)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<EquipmentBorrow> borrowPage = borrowRepository.findAll(spec, pageable);
        List<BorrowListResponse> list = borrowPage.getContent().stream().map(this::toBorrowListResponse).toList();

        return new PageResponse<>(list, borrowPage.getTotalElements(), pageNum, pageSize);
    }

    public BorrowDetailResponse getBorrowDetail(Long id) {
        EquipmentBorrow borrow = borrowRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("借用记录不存在"));

        // 权限：管理员可看全部；教师/学生只能看自己的
        if (!SecurityUtils.isAdmin()) {
            Long currentUserId = resolveCurrentUserId();
            if (currentUserId == null || !currentUserId.equals(borrow.getUserId())) {
                throw new BusinessException(403, "无权限查看该借用记录详情");
            }
        }
        return toBorrowDetailResponse(borrow);
    }

    @Transactional
    public BorrowDetailResponse createBorrow(CreateBorrowRequest request) {
        User currentUser = resolveCurrentUser();
        if (currentUser == null) {
            throw new BusinessException(401, "未授权访问，请先登录");
        }

        Equipment equipment = equipmentRepository.findById(request.getEquipmentId())
                .orElseThrow(() -> BusinessException.notFound("设备不存在"));

        // 校验日期
        if (request.getPlanReturnDate().isBefore(request.getBorrowDate())) {
            throw BusinessException.badRequest("参数错误：计划归还日期不能早于借用日期");
        }

        int qty = (request.getQuantity() == null || request.getQuantity() < 1) ? 1 : request.getQuantity();

        // 校验设备可借状态（按 equipment_status.code 粗略判定）
        EquipmentStatus status = equipmentStatusRepository.findById(equipment.getStatusId())
                .orElseThrow(() -> BusinessException.badRequest("设备状态不存在"));
        if (!isBorrowableEquipmentStatus(status.getCode())) {
            throw BusinessException.badRequest("设备不可用：该设备当前无法借用");
        }

        AvailableQuantityResponse available = getAvailableQuantityInternal(equipment);
        if (qty > available.getAvailableQuantity()) {
            throw BusinessException.badRequest("参数错误：借用数量不能超过设备可用数量");
        }

        EquipmentBorrow borrow = new EquipmentBorrow();
        borrow.setEquipmentId(request.getEquipmentId());
        borrow.setUserId(currentUser.getId());
        borrow.setBorrowDate(request.getBorrowDate());
        borrow.setPlanReturnDate(request.getPlanReturnDate());
        borrow.setPurpose(request.getPurpose());
        borrow.setQuantity(qty);
        borrow.setStatus(BorrowStatus.PENDING_APPROVAL);

        EquipmentBorrow saved = borrowRepository.save(borrow);
        return toBorrowDetailResponse(saved);
    }

    @Transactional
    public BorrowDetailResponse approveBorrow(Long id, ApproveBorrowRequest request) {
        EquipmentBorrow borrow = borrowRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("借用记录不存在"));
        if (borrow.getStatus() == null || borrow.getStatus() != BorrowStatus.PENDING_APPROVAL) {
            throw BusinessException.badRequest("该借用记录已审批过，无法重复审批");
        }
        if (request.getStatus() == null || (request.getStatus() != BorrowStatus.APPROVED && request.getStatus() != BorrowStatus.REJECTED)) {
            throw BusinessException.badRequest("参数错误：审批状态无效（必须是1-通过或2-拒绝）");
        }

        User approver = resolveCurrentUser();
        if (approver == null) {
            throw new BusinessException(401, "未授权访问，请先登录");
        }

        borrow.setStatus(request.getStatus());
        borrow.setApproverId(approver.getId());
        borrow.setApproveTime(LocalDateTime.now());
        borrow.setApproveRemark(StringUtils.hasText(request.getRemark()) ? request.getRemark() : null);

        EquipmentBorrow saved = borrowRepository.save(borrow);
        return toBorrowDetailResponse(saved);
    }

    @Transactional
    public BorrowDetailResponse confirmBorrow(Long id, ConfirmBorrowRequest request) {
        EquipmentBorrow borrow = borrowRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("借用记录不存在"));
        if (borrow.getStatus() == null || borrow.getStatus() != BorrowStatus.APPROVED) {
            throw BusinessException.badRequest("该借用记录状态不符合要求，无法确认借出");
        }

        Equipment equipment = equipmentRepository.findById(borrow.getEquipmentId())
                .orElseThrow(() -> BusinessException.notFound("设备不存在"));

        // 借出前再次校验可用数量（避免并发/后续申请导致超额）
        AvailableQuantityResponse available = getAvailableQuantityInternal(equipment);
        if (borrow.getQuantity() != null && borrow.getQuantity() > available.getAvailableQuantity()) {
            throw BusinessException.badRequest("参数错误：借用数量不能超过设备可用数量");
        }

        LocalDateTime actualBorrowDate = (request != null && request.getBorrowDate() != null) ? request.getBorrowDate() : LocalDateTime.now();
        borrow.setBorrowDate(actualBorrowDate);
        borrow.setStatus(BorrowStatus.BORROWED);

        EquipmentBorrow saved = borrowRepository.save(borrow);
        return toBorrowDetailResponse(saved);
    }

    @Transactional
    public BorrowDetailResponse returnBorrow(Long id, ReturnBorrowRequest request) {
        EquipmentBorrow borrow = borrowRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("借用记录不存在"));

        // 权限：管理员或借用人
        if (!SecurityUtils.isAdmin()) {
            Long currentUserId = resolveCurrentUserId();
            if (currentUserId == null || !currentUserId.equals(borrow.getUserId())) {
                throw new BusinessException(403, "无权限归还该设备");
            }
        }

        if (borrow.getStatus() == null || (borrow.getStatus() != BorrowStatus.BORROWED && borrow.getStatus() != BorrowStatus.OVERDUE)) {
            throw BusinessException.badRequest("该借用记录状态不符合要求，无法归还");
        }

        LocalDateTime actualReturn = (request != null && request.getActualReturnDate() != null) ? request.getActualReturnDate() : LocalDateTime.now();
        borrow.setActualReturnDate(actualReturn);
        borrow.setStatus(BorrowStatus.RETURNED);

        if (request != null && StringUtils.hasText(request.getRemark())) {
            String existing = borrow.getApproveRemark();
            String merged = StringUtils.hasText(existing) ? (existing + " | 归还备注：" + request.getRemark()) : ("归还备注：" + request.getRemark());
            borrow.setApproveRemark(merged);
        }

        EquipmentBorrow saved = borrowRepository.save(borrow);
        return toBorrowDetailResponse(saved);
    }

    @Transactional
    public MarkOverdueResponse markOverdue() {
        // 仅标记“已借出”且已超过计划归还日期的记录
        List<EquipmentBorrow> overdueList = borrowRepository.findByStatusAndPlanReturnDateBefore(BorrowStatus.BORROWED, LocalDateTime.now());
        int count = 0;
        for (EquipmentBorrow b : overdueList) {
            b.setStatus(BorrowStatus.OVERDUE);
            count++;
        }
        if (!overdueList.isEmpty()) {
            borrowRepository.saveAll(overdueList);
        }
        MarkOverdueResponse resp = new MarkOverdueResponse();
        resp.setOverdueCount(count);
        return resp;
    }

    public AvailableQuantityResponse getAvailableQuantity(Long equipmentId) {
        if (equipmentId == null) {
            throw BusinessException.badRequest("参数错误：设备ID不能为空");
        }
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> BusinessException.notFound("设备不存在"));
        return getAvailableQuantityInternal(equipment);
    }

    private AvailableQuantityResponse getAvailableQuantityInternal(Equipment equipment) {
        int total = equipment.getQuantity() == null ? 0 : equipment.getQuantity();
        int borrowed = borrowRepository.sumBorrowedQuantity(equipment.getId());
        int available = Math.max(0, total - borrowed);
        AvailableQuantityResponse resp = new AvailableQuantityResponse();
        resp.setEquipmentId(equipment.getId());
        resp.setTotalQuantity(total);
        resp.setBorrowedQuantity(borrowed);
        resp.setAvailableQuantity(available);
        return resp;
    }

    private boolean isBorrowableEquipmentStatus(String statusCode) {
        // 与现有设备状态码保持一致：instored/inuse 可借；其它默认不可借
        if (!StringUtils.hasText(statusCode)) return false;
        return "instored".equals(statusCode) || "inuse".equals(statusCode);
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

    private Long resolveEffectiveUserIdForQuery(Long userIdParam) {
        if (SecurityUtils.isAdmin()) {
            return userIdParam;
        }
        // 非管理员强制只能看自己的
        return resolveCurrentUserId();
    }

    private BorrowListResponse toBorrowListResponse(EquipmentBorrow borrow) {
        BorrowListResponse resp = new BorrowListResponse();
        fillCommon(resp, borrow);
        return resp;
    }

    private BorrowDetailResponse toBorrowDetailResponse(EquipmentBorrow borrow) {
        BorrowDetailResponse resp = new BorrowDetailResponse();
        fillCommon(resp, borrow);
        return resp;
    }

    private void fillCommon(Object respObj, EquipmentBorrow borrow) {
        Equipment equipment = equipmentRepository.findById(borrow.getEquipmentId()).orElse(null);
        User borrower = userRepository.findById(borrow.getUserId()).orElse(null);
        User approver = borrow.getApproverId() == null ? null : userRepository.findById(borrow.getApproverId()).orElse(null);

        if (respObj instanceof BorrowListResponse resp) {
            resp.setId(borrow.getId());
            resp.setEquipmentId(borrow.getEquipmentId());
            if (equipment != null) {
                resp.setEquipmentName(equipment.getName());
                resp.setEquipmentModel(equipment.getModel());
                resp.setEquipmentAssetCode(equipment.getAssetCode());
            }
            resp.setUserId(borrow.getUserId());
            if (borrower != null) {
                resp.setUserName(borrower.getName());
                resp.setUserDepartment(borrower.getDepartment());
            }
            resp.setBorrowDate(borrow.getBorrowDate());
            resp.setPlanReturnDate(borrow.getPlanReturnDate());
            resp.setActualReturnDate(borrow.getActualReturnDate());
            resp.setPurpose(borrow.getPurpose());
            resp.setQuantity(borrow.getQuantity());
            resp.setStatus(borrow.getStatus());
            resp.setStatusName(BorrowStatus.toName(borrow.getStatus()));
            resp.setApproverId(borrow.getApproverId());
            if (approver != null) {
                resp.setApproverName(approver.getName());
            }
            resp.setApproveTime(borrow.getApproveTime());
            resp.setApproveRemark(borrow.getApproveRemark());
            resp.setCreateTime(borrow.getCreateTime());
            resp.setUpdateTime(borrow.getUpdateTime());
        } else if (respObj instanceof BorrowDetailResponse resp) {
            resp.setId(borrow.getId());
            resp.setEquipmentId(borrow.getEquipmentId());
            if (equipment != null) {
                resp.setEquipmentName(equipment.getName());
                resp.setEquipmentModel(equipment.getModel());
                resp.setEquipmentAssetCode(equipment.getAssetCode());
            }
            resp.setUserId(borrow.getUserId());
            if (borrower != null) {
                resp.setUserName(borrower.getName());
                resp.setUserDepartment(borrower.getDepartment());
            }
            resp.setBorrowDate(borrow.getBorrowDate());
            resp.setPlanReturnDate(borrow.getPlanReturnDate());
            resp.setActualReturnDate(borrow.getActualReturnDate());
            resp.setPurpose(borrow.getPurpose());
            resp.setQuantity(borrow.getQuantity());
            resp.setStatus(borrow.getStatus());
            resp.setStatusName(BorrowStatus.toName(borrow.getStatus()));
            resp.setApproverId(borrow.getApproverId());
            if (approver != null) {
                resp.setApproverName(approver.getName());
            }
            resp.setApproveTime(borrow.getApproveTime());
            resp.setApproveRemark(borrow.getApproveRemark());
            resp.setCreateTime(borrow.getCreateTime());
            resp.setUpdateTime(borrow.getUpdateTime());
        }
    }
}


