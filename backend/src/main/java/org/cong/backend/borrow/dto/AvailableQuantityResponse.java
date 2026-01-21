package org.cong.backend.borrow.dto;

import lombok.Data;

@Data
public class AvailableQuantityResponse {
    private Long equipmentId;
    private Integer totalQuantity;
    private Integer borrowedQuantity;
    private Integer availableQuantity;
}


