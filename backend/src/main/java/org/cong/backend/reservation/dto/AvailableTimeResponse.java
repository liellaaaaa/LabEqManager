package org.cong.backend.reservation.dto;

import lombok.Data;

import java.util.List;

@Data
public class AvailableTimeResponse {
    private List<AvailableTimeSlot> availableTimeSlots;
}

