package org.cong.backend.reservation.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class ReservationListResponse {
    private Long id;
    private Long laboratoryId;
    private String laboratoryName;
    private String laboratoryCode;
    private Long userId;
    private String userName;
    private LocalDate reserveDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String purpose;
    private Integer status;
    private String statusName;
    private Long approverId;
    private String approverName;
    private LocalDateTime approveTime;
    private String approveRemark;
    private LocalTime actualStartTime;
    private LocalTime actualEndTime;
    private String usageRemark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

