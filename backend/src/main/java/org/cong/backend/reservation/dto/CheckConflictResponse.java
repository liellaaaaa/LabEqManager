package org.cong.backend.reservation.dto;

import lombok.Data;

import java.util.List;

@Data
public class CheckConflictResponse {
    private Boolean hasConflict;
    private List<ConflictInfo> conflictList;
}

