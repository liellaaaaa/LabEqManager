package org.cong.backend.borrow.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BorrowListResponse {
    private Long id;
    private Long equipmentId;
    private String equipmentName;
    private String equipmentModel;
    private String equipmentAssetCode;

    private Long userId;
    private String userName;
    private String userDepartment;

    private LocalDateTime borrowDate;
    private LocalDateTime planReturnDate;
    private LocalDateTime actualReturnDate;
    private String purpose;
    private Integer quantity;

    private Integer status;
    private String statusName;

    private Long approverId;
    private String approverName;
    private LocalDateTime approveTime;
    private String approveRemark;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}


