package org.cong.backend.scrap.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScrapStatsResponse {
    private Integer totalCount;
    private Integer pendingCount;
    private Integer approvedCount;
    private Integer rejectedCount;
    private Double approvalRate;
}

