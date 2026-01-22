package org.cong.backend.reservation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CreateReservationRequest {

    @NotNull(message = "实验室ID不能为空")
    private Long laboratoryId;

    @NotNull(message = "预约日期不能为空")
    private LocalDate reserveDate;

    @NotNull(message = "开始时间不能为空")
    private LocalTime startTime;

    @NotNull(message = "结束时间不能为空")
    private LocalTime endTime;

    private String purpose;
}

