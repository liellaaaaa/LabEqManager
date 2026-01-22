package org.cong.backend.reservation.dto;

import lombok.Data;

import java.time.LocalTime;

@Data
public class CompleteReservationRequest {
    private LocalTime actualStartTime;
    private LocalTime actualEndTime;
    private String usageRemark;
}

