package org.cong.backend.borrow.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "equipment_borrow")
public class EquipmentBorrow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "equipment_id", nullable = false)
    private Long equipmentId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "borrow_date", nullable = false)
    private LocalDateTime borrowDate;

    @Column(name = "plan_return_date", nullable = false)
    private LocalDateTime planReturnDate;

    @Column(name = "actual_return_date")
    private LocalDateTime actualReturnDate;

    @Column(length = 200)
    private String purpose;

    @Column(nullable = false)
    private Integer quantity = 1;

    /**
     * 0-待审批，1-已通过，2-已拒绝，3-已借出，4-已归还，5-已逾期
     */
    @Column(nullable = false)
    private Integer status = 0;

    @Column(name = "approver_id")
    private Long approverId;

    @Column(name = "approve_time")
    private LocalDateTime approveTime;

    @Column(name = "approve_remark", length = 200)
    private String approveRemark;

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


