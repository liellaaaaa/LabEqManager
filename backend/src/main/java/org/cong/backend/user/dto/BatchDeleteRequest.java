package org.cong.backend.user.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BatchDeleteRequest {
    @NotEmpty(message = "用户ID列表不能为空")
    private List<Long> ids;
}

