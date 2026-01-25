package org.cong.backend.scrap.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ScrapListResponse {
    private Long id;
    private Long equipmentId;
    private String equipmentName;
    private String equipmentModel;
    private String equipmentAssetCode;
    private Long applicantId;
    private String applicantName;
    private LocalDateTime applyDate;
    private String scrapReason;
    private Integer status;
    private String statusName;
    private Long approverId;
    private String approverName;
    private LocalDateTime approveTime;
    private String approveRemark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

