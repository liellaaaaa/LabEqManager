package org.cong.backend.reservation.repository;

import org.cong.backend.reservation.entity.LaboratoryReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface LaboratoryReservationRepository extends JpaRepository<LaboratoryReservation, Long>, JpaSpecificationExecutor<LaboratoryReservation> {

    /**
     * 查找指定实验室在指定日期和时间段内已通过或已使用的预约记录（用于冲突检测）
     * 状态：1-已通过，5-已使用
     * 冲突判断：新预约的开始时间 < 已有预约的结束时间 AND 新预约的结束时间 > 已有预约的开始时间
     */
    @Query("SELECT r FROM LaboratoryReservation r WHERE r.laboratoryId = :laboratoryId " +
           "AND r.reserveDate = :reserveDate " +
           "AND r.status IN (1, 5) " +
           "AND (r.startTime < :endTime AND r.endTime > :startTime) " +
           "AND (:excludeId IS NULL OR r.id != :excludeId)")
    List<LaboratoryReservation> findConflictingReservations(
            @Param("laboratoryId") Long laboratoryId,
            @Param("reserveDate") LocalDate reserveDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") Long excludeId);

    /**
     * 查找指定实验室在指定日期的所有已通过或已使用的预约记录（用于获取可用时间段）
     * 状态：1-已通过，5-已使用
     */
    @Query("SELECT r FROM LaboratoryReservation r WHERE r.laboratoryId = :laboratoryId " +
           "AND r.reserveDate = :reserveDate " +
           "AND r.status IN (1, 5) " +
           "ORDER BY r.startTime ASC")
    List<LaboratoryReservation> findByLaboratoryIdAndReserveDateAndApprovedStatus(
            @Param("laboratoryId") Long laboratoryId,
            @Param("reserveDate") LocalDate reserveDate);
}

