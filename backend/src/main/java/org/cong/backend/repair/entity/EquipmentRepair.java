package org.cong.backend.repair.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "equipment_repair")
public class EquipmentRepair {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "equipment_id", nullable = false)
    private Long equipmentId;

    @Column(name = "reporter_id", nullable = false)
    private Long reporterId;

    @Column(name = "report_date", nullable = false)
    private LocalDateTime reportDate;

    @Column(name = "fault_description", nullable = false, columnDefinition = "TEXT")
    private String faultDescription;

    @Column(name = "repair_result", columnDefinition = "TEXT")
    private String repairResult;

    @Column(name = "repair_date")
    private LocalDateTime repairDate;

    /**
     * 0-待维修，1-维修中，2-已修好，3-无法修复
     */
    @Column(nullable = false)
    private Integer status = 0;

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

