package org.cong.backend.reservation.service;

import org.cong.backend.common.BusinessException;
import org.cong.backend.laboratory.entity.Laboratory;
import org.cong.backend.laboratory.repository.LaboratoryRepository;
import org.cong.backend.reservation.dto.*;
import org.cong.backend.reservation.entity.LaboratoryReservation;
import org.cong.backend.reservation.repository.LaboratoryReservationRepository;
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
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LaboratoryReservationService {

    private final LaboratoryReservationRepository reservationRepository;
    private final LaboratoryRepository laboratoryRepository;
    private final UserRepository userRepository;

    public LaboratoryReservationService(LaboratoryReservationRepository reservationRepository,
                                      LaboratoryRepository laboratoryRepository,
                                      UserRepository userRepository) {
        this.reservationRepository = reservationRepository;
        this.laboratoryRepository = laboratoryRepository;
        this.userRepository = userRepository;
    }

    public PageResponse<ReservationListResponse> getReservationList(Integer page, Integer size,
                                                                   Long laboratoryId, Long userId,
                                                                   LocalDate reserveDate, Integer status,
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

        Specification<LaboratoryReservation> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (laboratoryId != null) {
                predicates.add(cb.equal(root.get("laboratoryId"), laboratoryId));
            }
            if (effectiveUserId != null) {
                predicates.add(cb.equal(root.get("userId"), effectiveUserId));
            }
            if (reserveDate != null) {
                predicates.add(cb.equal(root.get("reserveDate"), reserveDate));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<LaboratoryReservation> reservationPage = reservationRepository.findAll(spec, pageable);
        List<ReservationListResponse> list = reservationPage.getContent().stream()
                .map(this::toReservationListResponse).toList();

        return new PageResponse<>(list, reservationPage.getTotalElements(), pageNum, pageSize);
    }

    public ReservationDetailResponse getReservationDetail(Long id) {
        LaboratoryReservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("预约记录不存在"));

        // 权限：管理员可看全部；教师/学生只能看自己的
        if (!SecurityUtils.isAdmin()) {
            Long currentUserId = resolveCurrentUserId();
            if (currentUserId == null || !currentUserId.equals(reservation.getUserId())) {
                throw new BusinessException(403, "无权限查看该预约详情");
            }
        }
        return toReservationDetailResponse(reservation);
    }

    @Transactional
    public ReservationDetailResponse createReservation(CreateReservationRequest request) {
        User currentUser = resolveCurrentUser();
        if (currentUser == null) {
            throw new BusinessException(401, "未授权访问，请先登录");
        }

        Laboratory laboratory = laboratoryRepository.findById(request.getLaboratoryId())
                .orElseThrow(() -> BusinessException.notFound("实验室不存在"));

        // 校验实验室状态
        if (laboratory.getStatus() == null || laboratory.getStatus() != 1) {
            throw BusinessException.badRequest("实验室不可用：该实验室当前无法预约");
        }

        // 校验时间
        if (request.getEndTime().isBefore(request.getStartTime()) || 
            request.getEndTime().equals(request.getStartTime())) {
            throw BusinessException.badRequest("参数错误：结束时间不能早于或等于开始时间");
        }

        // 校验预约日期不能是过去日期
        if (request.getReserveDate().isBefore(LocalDate.now())) {
            throw BusinessException.badRequest("参数错误：预约日期不能是过去日期");
        }

        // 冲突检测
        List<LaboratoryReservation> conflicts = reservationRepository.findConflictingReservations(
                request.getLaboratoryId(),
                request.getReserveDate(),
                request.getStartTime(),
                request.getEndTime(),
                null
        );
        if (!conflicts.isEmpty()) {
            throw BusinessException.badRequest("预约冲突：该时间段实验室已被预约");
        }

        LaboratoryReservation reservation = new LaboratoryReservation();
        reservation.setLaboratoryId(request.getLaboratoryId());
        reservation.setUserId(currentUser.getId());
        reservation.setReserveDate(request.getReserveDate());
        reservation.setStartTime(request.getStartTime());
        reservation.setEndTime(request.getEndTime());
        reservation.setPurpose(request.getPurpose());
        reservation.setStatus(ReservationStatus.PENDING_APPROVAL);

        LaboratoryReservation saved = reservationRepository.save(reservation);
        return toReservationDetailResponse(saved);
    }

    @Transactional
    public ReservationDetailResponse cancelReservation(Long id, CancelReservationRequest request) {
        LaboratoryReservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("预约记录不存在"));

        // 权限：管理员或预约人
        if (!SecurityUtils.isAdmin()) {
            Long currentUserId = resolveCurrentUserId();
            if (currentUserId == null || !currentUserId.equals(reservation.getUserId())) {
                throw new BusinessException(403, "无权限取消该预约");
            }
        }

        // 状态校验：只有待审批或已通过的预约可以取消
        if (reservation.getStatus() == null || 
            (reservation.getStatus() != ReservationStatus.PENDING_APPROVAL && 
             reservation.getStatus() != ReservationStatus.APPROVED)) {
            throw BusinessException.badRequest("该预约已无法取消");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        if (StringUtils.hasText(request.getRemark())) {
            String existing = reservation.getApproveRemark();
            String merged = StringUtils.hasText(existing) ? 
                    (existing + " | 取消备注：" + request.getRemark()) : 
                    ("取消备注：" + request.getRemark());
            reservation.setApproveRemark(merged);
        }

        LaboratoryReservation saved = reservationRepository.save(reservation);
        return toReservationDetailResponse(saved);
    }

    @Transactional
    public ReservationDetailResponse approveReservation(Long id, ApproveReservationRequest request) {
        LaboratoryReservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("预约记录不存在"));

        // 状态校验：只有待审批的预约可以审批
        if (reservation.getStatus() == null || reservation.getStatus() != ReservationStatus.PENDING_APPROVAL) {
            throw BusinessException.badRequest("该预约已审批过，无法重复审批");
        }

        // 审批状态校验
        if (request.getStatus() == null || 
            (request.getStatus() != ReservationStatus.APPROVED && request.getStatus() != ReservationStatus.REJECTED)) {
            throw BusinessException.badRequest("参数错误：审批状态无效（必须是1-通过或2-拒绝）");
        }

        // 如果审批通过，再次检查冲突（防止审批期间产生新冲突）
        if (request.getStatus() == ReservationStatus.APPROVED) {
            List<LaboratoryReservation> conflicts = reservationRepository.findConflictingReservations(
                    reservation.getLaboratoryId(),
                    reservation.getReserveDate(),
                    reservation.getStartTime(),
                    reservation.getEndTime(),
                    reservation.getId()
            );
            if (!conflicts.isEmpty()) {
                throw BusinessException.badRequest("预约冲突：该时间段实验室已被预约");
            }
        }

        User approver = resolveCurrentUser();
        if (approver == null) {
            throw new BusinessException(401, "未授权访问，请先登录");
        }

        reservation.setStatus(request.getStatus());
        reservation.setApproverId(approver.getId());
        reservation.setApproveTime(LocalDateTime.now());
        reservation.setApproveRemark(StringUtils.hasText(request.getRemark()) ? request.getRemark() : null);

        LaboratoryReservation saved = reservationRepository.save(reservation);
        return toReservationDetailResponse(saved);
    }

    @Transactional
    public ReservationDetailResponse completeReservation(Long id, CompleteReservationRequest request) {
        LaboratoryReservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("预约记录不存在"));

        // 状态校验：只有已通过的预约可以标记为完成
        if (reservation.getStatus() == null || reservation.getStatus() != ReservationStatus.APPROVED) {
            throw BusinessException.badRequest("该预约状态不符合要求，无法标记为完成");
        }

        // 校验实际时间
        if (request.getActualStartTime() != null && request.getActualEndTime() != null) {
            if (request.getActualEndTime().isBefore(request.getActualStartTime()) ||
                request.getActualEndTime().equals(request.getActualStartTime())) {
                throw BusinessException.badRequest("参数错误：实际结束时间不能早于或等于实际开始时间");
            }
        }

        reservation.setStatus(ReservationStatus.COMPLETED);
        if (request.getActualStartTime() != null) {
            reservation.setActualStartTime(request.getActualStartTime());
        }
        if (request.getActualEndTime() != null) {
            reservation.setActualEndTime(request.getActualEndTime());
        }
        if (StringUtils.hasText(request.getUsageRemark())) {
            reservation.setUsageRemark(request.getUsageRemark());
        }

        LaboratoryReservation saved = reservationRepository.save(reservation);
        return toReservationDetailResponse(saved);
    }

    public CheckConflictResponse checkConflict(CheckConflictRequest request) {
        Laboratory laboratory = laboratoryRepository.findById(request.getLaboratoryId())
                .orElseThrow(() -> BusinessException.notFound("实验室不存在"));

        // 校验时间
        if (request.getEndTime().isBefore(request.getStartTime()) || 
            request.getEndTime().equals(request.getStartTime())) {
            throw BusinessException.badRequest("参数错误：结束时间不能早于或等于开始时间");
        }

        List<LaboratoryReservation> conflicts = reservationRepository.findConflictingReservations(
                request.getLaboratoryId(),
                request.getReserveDate(),
                request.getStartTime(),
                request.getEndTime(),
                request.getExcludeId()
        );

        CheckConflictResponse response = new CheckConflictResponse();
        response.setHasConflict(!conflicts.isEmpty());
        if (!conflicts.isEmpty()) {
            List<ConflictInfo> conflictList = conflicts.stream().map(conflict -> {
                ConflictInfo info = new ConflictInfo();
                info.setId(conflict.getId());
                info.setStartTime(conflict.getStartTime());
                info.setEndTime(conflict.getEndTime());
                info.setStatus(conflict.getStatus());
                return info;
            }).collect(Collectors.toList());
            response.setConflictList(conflictList);
        }
        return response;
    }

    public AvailableTimeResponse getAvailableTime(Long laboratoryId, LocalDate reserveDate) {
        Laboratory laboratory = laboratoryRepository.findById(laboratoryId)
                .orElseThrow(() -> BusinessException.notFound("实验室不存在"));

        // 获取该日期所有已通过或已使用的预约
        List<LaboratoryReservation> reservations = reservationRepository.findByLaboratoryIdAndReserveDateAndApprovedStatus(
                laboratoryId, reserveDate);

        // 生成可用时间段（简化版：假设实验室开放时间为 08:00-22:00）
        LocalTime openTime = LocalTime.of(8, 0);
        LocalTime closeTime = LocalTime.of(22, 0);

        List<AvailableTimeSlot> availableSlots = new ArrayList<>();
        LocalTime current = openTime;

        // 按开始时间排序预约
        List<LaboratoryReservation> sortedReservations = reservations.stream()
                .sorted((a, b) -> a.getStartTime().compareTo(b.getStartTime()))
                .collect(Collectors.toList());

        for (LaboratoryReservation reservation : sortedReservations) {
            if (current.isBefore(reservation.getStartTime())) {
                // 当前时间到预约开始时间之间有空闲
                availableSlots.add(createTimeSlot(current, reservation.getStartTime()));
            }
            // 更新当前时间为预约结束时间
            if (reservation.getEndTime().isAfter(current)) {
                current = reservation.getEndTime();
            }
        }

        // 处理最后一段
        if (current.isBefore(closeTime)) {
            availableSlots.add(createTimeSlot(current, closeTime));
        }

        AvailableTimeResponse response = new AvailableTimeResponse();
        response.setAvailableTimeSlots(availableSlots);
        return response;
    }

    private AvailableTimeSlot createTimeSlot(LocalTime start, LocalTime end) {
        AvailableTimeSlot slot = new AvailableTimeSlot();
        slot.setStartTime(start);
        slot.setEndTime(end);
        return slot;
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

    private ReservationListResponse toReservationListResponse(LaboratoryReservation reservation) {
        ReservationListResponse resp = new ReservationListResponse();
        fillCommon(resp, reservation);
        return resp;
    }

    private ReservationDetailResponse toReservationDetailResponse(LaboratoryReservation reservation) {
        ReservationDetailResponse resp = new ReservationDetailResponse();
        fillCommon(resp, reservation);
        return resp;
    }

    private void fillCommon(Object respObj, LaboratoryReservation reservation) {
        Laboratory laboratory = laboratoryRepository.findById(reservation.getLaboratoryId()).orElse(null);
        User user = userRepository.findById(reservation.getUserId()).orElse(null);
        User approver = reservation.getApproverId() == null ? null : 
                userRepository.findById(reservation.getApproverId()).orElse(null);

        if (respObj instanceof ReservationListResponse resp) {
            resp.setId(reservation.getId());
            resp.setLaboratoryId(reservation.getLaboratoryId());
            if (laboratory != null) {
                resp.setLaboratoryName(laboratory.getName());
                resp.setLaboratoryCode(laboratory.getCode());
            }
            resp.setUserId(reservation.getUserId());
            if (user != null) {
                resp.setUserName(user.getName());
            }
            resp.setReserveDate(reservation.getReserveDate());
            resp.setStartTime(reservation.getStartTime());
            resp.setEndTime(reservation.getEndTime());
            resp.setPurpose(reservation.getPurpose());
            resp.setStatus(reservation.getStatus());
            resp.setStatusName(ReservationStatus.toName(reservation.getStatus()));
            resp.setApproverId(reservation.getApproverId());
            if (approver != null) {
                resp.setApproverName(approver.getName());
            }
            resp.setApproveTime(reservation.getApproveTime());
            resp.setApproveRemark(reservation.getApproveRemark());
            resp.setActualStartTime(reservation.getActualStartTime());
            resp.setActualEndTime(reservation.getActualEndTime());
            resp.setUsageRemark(reservation.getUsageRemark());
            resp.setCreateTime(reservation.getCreateTime());
            resp.setUpdateTime(reservation.getUpdateTime());
        } else if (respObj instanceof ReservationDetailResponse resp) {
            resp.setId(reservation.getId());
            resp.setLaboratoryId(reservation.getLaboratoryId());
            if (laboratory != null) {
                resp.setLaboratoryName(laboratory.getName());
                resp.setLaboratoryCode(laboratory.getCode());
            }
            resp.setUserId(reservation.getUserId());
            if (user != null) {
                resp.setUserName(user.getName());
            }
            resp.setReserveDate(reservation.getReserveDate());
            resp.setStartTime(reservation.getStartTime());
            resp.setEndTime(reservation.getEndTime());
            resp.setPurpose(reservation.getPurpose());
            resp.setStatus(reservation.getStatus());
            resp.setStatusName(ReservationStatus.toName(reservation.getStatus()));
            resp.setApproverId(reservation.getApproverId());
            if (approver != null) {
                resp.setApproverName(approver.getName());
            }
            resp.setApproveTime(reservation.getApproveTime());
            resp.setApproveRemark(reservation.getApproveRemark());
            resp.setActualStartTime(reservation.getActualStartTime());
            resp.setActualEndTime(reservation.getActualEndTime());
            resp.setUsageRemark(reservation.getUsageRemark());
            resp.setCreateTime(reservation.getCreateTime());
            resp.setUpdateTime(reservation.getUpdateTime());
        }
    }
}

