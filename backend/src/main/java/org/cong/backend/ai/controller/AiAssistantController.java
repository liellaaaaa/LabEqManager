package org.cong.backend.ai.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.cong.backend.ai.dto.ChatRequest;
import org.cong.backend.ai.dto.ChatResponse;
import org.cong.backend.ai.service.AiAssistantService;
import org.cong.backend.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "AI 智能助手", description = "AI 智能助手相关接口")
public class AiAssistantController {

    private final AiAssistantService aiAssistantService;

    public AiAssistantController(AiAssistantService aiAssistantService) {
        this.aiAssistantService = aiAssistantService;
    }

    @PostMapping("/chat")
    @PreAuthorize("hasAnyRole('admin', 'teacher', 'student')")
    @Operation(summary = "AI 对话", description = "发送消息给 AI 助手，获取回答。优先使用知识库，未匹配则调用通义千问 API。")
    public ResponseEntity<ApiResponse<ChatResponse>> chat(
            @Validated @RequestBody ChatRequest request) {
        ChatResponse response = aiAssistantService.chat(request);
        return ResponseEntity.ok(ApiResponse.success("获取成功", response));
    }
}

