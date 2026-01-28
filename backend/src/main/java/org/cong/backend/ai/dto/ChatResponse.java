package org.cong.backend.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    /**
     * AI 回答内容
     */
    private String answer;

    /**
     * 回答来源：knowledge（知识库）或 api（通义千问API）
     */
    private String source;
}

