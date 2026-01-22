package org.cong.backend.reservation.dto;

import lombok.Data;

import java.time.LocalTime;

@Data
public class ConflictInfo {
    private Long id;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer status;
}

