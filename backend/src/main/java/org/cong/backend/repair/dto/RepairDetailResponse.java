package org.cong.backend.repair.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RepairDetailResponse {
    private Long id;
    private Long equipmentId;
    private String equipmentName;
    private String equipmentModel;
    private String equipmentAssetCode;
    private Long reporterId;
    private String reporterName;
    private LocalDateTime reportDate;
    private String faultDescription;
    private String repairResult;
    private LocalDateTime repairDate;
    private Integer status;
    private String statusName;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

