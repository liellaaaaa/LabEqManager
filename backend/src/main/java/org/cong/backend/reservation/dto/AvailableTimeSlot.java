package org.cong.backend.reservation.dto;

import lombok.Data;

import java.time.LocalTime;

@Data
public class AvailableTimeSlot {
    private LocalTime startTime;
    private LocalTime endTime;
}

