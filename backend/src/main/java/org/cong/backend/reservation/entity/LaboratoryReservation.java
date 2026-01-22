package org.cong.backend.reservation.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "laboratory_reservation")
public class LaboratoryReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "laboratory_id", nullable = false)
    private Long laboratoryId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "reserve_date", nullable = false)
    private LocalDate reserveDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(length = 200)
    private String purpose;

    /**
     * 0-待审批，1-已通过，2-已拒绝，3-已取消，4-已完成，5-已使用
     */
    @Column(nullable = false)
    private Integer status = 0;

    @Column(name = "approver_id")
    private Long approverId;

    @Column(name = "approve_time")
    private LocalDateTime approveTime;

    @Column(name = "approve_remark", length = 200)
    private String approveRemark;

    @Column(name = "actual_start_time")
    private LocalTime actualStartTime;

    @Column(name = "actual_end_time")
    private LocalTime actualEndTime;

    @Column(name = "usage_remark", length = 200)
    private String usageRemark;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createTime = now;
        this.updateTime = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }
}

