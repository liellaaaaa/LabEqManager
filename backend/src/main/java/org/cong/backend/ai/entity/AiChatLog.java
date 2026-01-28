package org.cong.backend.ai.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ai_chat_log", indexes = {
    @Index(name = "idx_user_date", columnList = "user_id,log_date"),
    @Index(name = "idx_log_date", columnList = "log_date")
})
public class AiChatLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_input", nullable = false, columnDefinition = "TEXT")
    private String userInput;

    @Column(name = "ai_output", nullable = false, columnDefinition = "TEXT")
    private String aiOutput;

    /**
     * 回答来源：knowledge（知识库）或 api（通义千问API）
     */
    @Column(name = "source", nullable = false, length = 20)
    private String source;

    /**
     * 日志日期（用于按自然日划分）
     */
    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createTime = now;
        if (this.logDate == null) {
            this.logDate = now.toLocalDate();
        }
    }
}

