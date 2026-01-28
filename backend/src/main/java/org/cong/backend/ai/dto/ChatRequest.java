package org.cong.backend.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatRequest {

    @NotBlank(message = "输入内容不能为空")
    private String message;
}

