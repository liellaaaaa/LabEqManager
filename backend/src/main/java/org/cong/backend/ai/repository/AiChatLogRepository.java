package org.cong.backend.ai.repository;

import org.cong.backend.ai.entity.AiChatLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AiChatLogRepository extends JpaRepository<AiChatLog, Long> {

    /**
     * 根据用户ID和日期查询日志
     */
    List<AiChatLog> findByUserIdAndLogDate(Long userId, LocalDate logDate);

    /**
     * 根据日期查询日志
     */
    List<AiChatLog> findByLogDate(LocalDate logDate);
}

