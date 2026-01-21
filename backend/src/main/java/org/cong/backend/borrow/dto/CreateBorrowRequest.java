package org.cong.backend.borrow.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateBorrowRequest {

    @NotNull(message = "设备ID不能为空")
    private Long equipmentId;

    @NotNull(message = "借用日期不能为空")
    private LocalDateTime borrowDate;

    @NotNull(message = "计划归还日期不能为空")
    private LocalDateTime planReturnDate;

    private String purpose;

    private Integer quantity = 1;
}


