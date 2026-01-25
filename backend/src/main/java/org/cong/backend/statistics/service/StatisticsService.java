package org.cong.backend.statistics.service;

import org.cong.backend.borrow.entity.EquipmentBorrow;
import org.cong.backend.borrow.repository.EquipmentBorrowRepository;
import org.cong.backend.equipment.entity.Equipment;
import org.cong.backend.equipment.repository.EquipmentRepository;
import org.cong.backend.laboratory.entity.Laboratory;
import org.cong.backend.laboratory.repository.LaboratoryRepository;
import org.cong.backend.reservation.entity.LaboratoryReservation;
import org.cong.backend.reservation.repository.LaboratoryReservationRepository;
import org.cong.backend.security.SecurityUtils;
import org.cong.backend.statistics.dto.BorrowStatsResponse;
import org.cong.backend.statistics.dto.EquipmentUsageStatsResponse;
import org.cong.backend.statistics.dto.ReminderResponse;
import org.cong.backend.user.entity.User;
import org.cong.backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    private final EquipmentRepository equipmentRepository;
    private final EquipmentBorrowRepository borrowRepository;
    private final LaboratoryReservationRepository reservationRepository;
    private final LaboratoryRepository laboratoryRepository;
    private final UserRepository userRepository;

    // 提醒提前天数（预约到期前N天提醒）
    private static final int REMINDER_DAYS_BEFORE = 1;

    public StatisticsService(EquipmentRepository equipmentRepository,
                           EquipmentBorrowRepository borrowRepository,
                           LaboratoryReservationRepository reservationRepository,
                           LaboratoryRepository laboratoryRepository,
                           UserRepository userRepository) {
        this.equipmentRepository = equipmentRepository;
        this.borrowRepository = borrowRepository;
        this.reservationRepository = reservationRepository;
        this.laboratoryRepository = laboratoryRepository;
        this.userRepository = userRepository;
    }

    /**
     * 获取设备使用次数统计
     * 统计每个设备被借用的次数（已归还的记录）
     */
    public EquipmentUsageStatsResponse getEquipmentUsageStats() {
        // 获取所有设备
        List<Equipment> equipmentList = equipmentRepository.findAll();
        
        // 获取所有已归还的借用记录（状态为4-已归还）
        List<EquipmentBorrow> returnedBorrows = borrowRepository.findAll().stream()
                .filter(b -> b.getStatus() != null && b.getStatus() == 4)
                .toList();
        
        // 按设备ID分组统计使用次数
        Map<Long, Long> usageCountMap = returnedBorrows.stream()
                .collect(Collectors.groupingBy(
                        EquipmentBorrow::getEquipmentId,
                        Collectors.counting()
                ));
        
        // 构建响应列表
        List<EquipmentUsageStatsResponse.EquipmentUsageItem> items = equipmentList.stream()
                .map(equipment -> {
                    EquipmentUsageStatsResponse.EquipmentUsageItem item = 
                            new EquipmentUsageStatsResponse.EquipmentUsageItem();
                    item.setEquipmentId(equipment.getId());
                    item.setEquipmentName(equipment.getName());
                    item.setEquipmentModel(equipment.getModel());
                    item.setAssetCode(equipment.getAssetCode());
                    item.setUsageCount(usageCountMap.getOrDefault(equipment.getId(), 0L));
                    return item;
                })
                .sorted((a, b) -> Long.compare(b.getUsageCount(), a.getUsageCount())) // 按使用次数降序
                .toList();
        
        long totalUsageCount = usageCountMap.values().stream()
                .mapToLong(Long::longValue)
                .sum();
        
        EquipmentUsageStatsResponse response = new EquipmentUsageStatsResponse();
        response.setEquipmentList(items);
        response.setTotalEquipmentCount((long) equipmentList.size());
        response.setTotalUsageCount(totalUsageCount);
        
        return response;
    }

    /**
     * 获取借用/逾期统计
     */
    public BorrowStatsResponse getBorrowStats() {
        List<EquipmentBorrow> allBorrows = borrowRepository.findAll();
        
        BorrowStatsResponse response = new BorrowStatsResponse();
        response.setTotalBorrowCount((long) allBorrows.size());
        response.setPendingCount((long) allBorrows.stream()
                .filter(b -> b.getStatus() != null && b.getStatus() == 0)
                .count());
        response.setApprovedCount((long) allBorrows.stream()
                .filter(b -> b.getStatus() != null && b.getStatus() == 1)
                .count());
        response.setBorrowedCount((long) allBorrows.stream()
                .filter(b -> b.getStatus() != null && b.getStatus() == 3)
                .count());
        response.setReturnedCount((long) allBorrows.stream()
                .filter(b -> b.getStatus() != null && b.getStatus() == 4)
                .count());
        response.setOverdueCount((long) allBorrows.stream()
                .filter(b -> b.getStatus() != null && b.getStatus() == 5)
                .count());
        response.setRejectedCount((long) allBorrows.stream()
                .filter(b -> b.getStatus() != null && b.getStatus() == 2)
                .count());
        
        return response;
    }

    /**
     * 获取到期提醒
     * 返回当前用户即将到期的借用和预约记录
     */
    public ReminderResponse getReminders() {
        Long currentUserId = resolveCurrentUserId();
        if (currentUserId == null) {
            // 未登录用户返回空提醒
            ReminderResponse response = new ReminderResponse();
            response.setHasReminder(false);
            response.setReminderCount(0);
            response.setBorrowReminders(new ArrayList<>());
            response.setReservationReminders(new ArrayList<>());
            return response;
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();
        LocalDate reminderThreshold = today.plusDays(REMINDER_DAYS_BEFORE);
        
        // 获取当前用户的借用提醒
        List<ReminderResponse.BorrowReminderItem> borrowReminders = new ArrayList<>();
        List<EquipmentBorrow> userBorrows = borrowRepository.findAll().stream()
                .filter(b -> b.getUserId().equals(currentUserId))
                .filter(b -> {
                    // 只包含已借出（状态3）或已逾期（状态5）的记录
                    Integer status = b.getStatus();
                    return status != null && (status == 3 || status == 5);
                })
                .filter(b -> {
                    // 只包含计划归还日期在提醒阈值之前的记录
                    if (b.getPlanReturnDate() == null) return false;
                    LocalDate planReturnDate = b.getPlanReturnDate().toLocalDate();
                    return !planReturnDate.isAfter(reminderThreshold);
                })
                .toList();
        
        for (EquipmentBorrow borrow : userBorrows) {
            Equipment equipment = equipmentRepository.findById(borrow.getEquipmentId()).orElse(null);
            if (equipment == null) continue;
            
            ReminderResponse.BorrowReminderItem item = new ReminderResponse.BorrowReminderItem();
            item.setId(borrow.getId());
            item.setEquipmentName(equipment.getName());
            item.setPlanReturnDate(borrow.getPlanReturnDate());
            
            LocalDate planReturnDate = borrow.getPlanReturnDate().toLocalDate();
            long remainingDays = ChronoUnit.DAYS.between(today, planReturnDate);
            item.setRemainingDays(remainingDays);
            item.setIsOverdue(remainingDays < 0);
            
            borrowReminders.add(item);
        }
        
        // 获取当前用户的预约提醒
        List<ReminderResponse.ReservationReminderItem> reservationReminders = new ArrayList<>();
        List<LaboratoryReservation> userReservations = reservationRepository.findAll().stream()
                .filter(r -> r.getUserId().equals(currentUserId))
                .filter(r -> {
                    // 只包含已通过（状态1）的预约
                    Integer status = r.getStatus();
                    return status != null && status == 1;
                })
                .filter(r -> {
                    // 只包含预约日期在提醒阈值之前的记录
                    if (r.getReserveDate() == null) return false;
                    return !r.getReserveDate().isAfter(reminderThreshold);
                })
                .toList();
        
        for (LaboratoryReservation reservation : userReservations) {
            Laboratory laboratory = reservation.getLaboratoryId() != null ?
                    laboratoryRepository.findById(reservation.getLaboratoryId()).orElse(null) : null;
            
            ReminderResponse.ReservationReminderItem item = 
                    new ReminderResponse.ReservationReminderItem();
            item.setId(reservation.getId());
            item.setLaboratoryName(laboratory != null ? laboratory.getName() : "未知实验室");
            item.setReserveDate(reservation.getReserveDate());
            item.setStartTime(reservation.getStartTime());
            item.setEndTime(reservation.getEndTime());
            
            long remainingDays = ChronoUnit.DAYS.between(today, reservation.getReserveDate());
            item.setRemainingDays(remainingDays);
            item.setIsExpired(remainingDays < 0);
            
            reservationReminders.add(item);
        }
        
        int totalReminderCount = borrowReminders.size() + reservationReminders.size();
        
        ReminderResponse response = new ReminderResponse();
        response.setHasReminder(totalReminderCount > 0);
        response.setReminderCount(totalReminderCount);
        response.setBorrowReminders(borrowReminders);
        response.setReservationReminders(reservationReminders);
        
        return response;
    }

    private Long resolveCurrentUserId() {
        String username = SecurityUtils.getCurrentUsername();
        if (!StringUtils.hasText(username)) return null;
        return userRepository.findByUsername(username)
                .map(User::getId)
                .orElse(null);
    }
}

