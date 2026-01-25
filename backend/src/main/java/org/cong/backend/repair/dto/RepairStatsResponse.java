package org.cong.backend.repair.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RepairStatsResponse {
    private Integer totalCount;
    private Integer pendingCount;
    private Integer repairingCount;
    private Integer fixedCount;
    private Integer unrepairableCount;
    private Double repairRate;
}

