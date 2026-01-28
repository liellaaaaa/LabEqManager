package org.cong.backend.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cong.backend.ai.dto.ChatRequest;
import org.cong.backend.ai.entity.AiChatLog;
import org.cong.backend.ai.repository.AiChatLogRepository;
import org.cong.backend.user.entity.User;
import org.cong.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=update"
})
@AutoConfigureMockMvc
@Transactional
class AiAssistantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AiChatLogRepository chatLogRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // 创建测试用户
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("$2a$10$dummy"); // 加密后的密码
        testUser.setName("测试用户");
        testUser.setStatus(1);
        testUser.setCreateTime(LocalDateTime.now());
        testUser.setUpdateTime(LocalDateTime.now());
        testUser = userRepository.save(testUser);
    }

    @Test
    @DisplayName("知识库匹配测试：完全匹配 - 如何借用设备")
    @WithMockUser(username = "testuser", roles = {"student"})
    void testChatWithKnowledgeBaseExactMatch() throws Exception {
        ChatRequest request = new ChatRequest();
        request.setMessage("如何借用设备");

        mockMvc.perform(post("/api/v1/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.answer").exists())
                .andExpect(jsonPath("$.data.answer").value(containsString("设备借用流程")))
                .andExpect(jsonPath("$.data.source").value("knowledge"))
                .andReturn();

        // 验证日志已保存
        Optional<AiChatLog> log = chatLogRepository.findByUserIdAndLogDate(
                testUser.getId(), java.time.LocalDate.now())
                .stream()
                .filter(l -> l.getUserInput().equals("如何借用设备"))
                .findFirst();
        assertTrue(log.isPresent());
        assertEquals("knowledge", log.get().getSource());
    }

    @Test
    @DisplayName("知识库匹配测试：包含匹配 - 设备借用流程")
    @WithMockUser(username = "testuser", roles = {"student"})
    void testChatWithKnowledgeBasePartialMatch() throws Exception {
        ChatRequest request = new ChatRequest();
        request.setMessage("我想了解一下设备借用的流程");

        mockMvc.perform(post("/api/v1/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.answer").exists())
                .andExpect(jsonPath("$.data.source").value("knowledge"));
    }

    @Test
    @DisplayName("知识库匹配测试：关键词匹配 - 实验室预约")
    @WithMockUser(username = "testuser", roles = {"student"})
    void testChatWithKnowledgeBaseKeywordMatch() throws Exception {
        ChatRequest request = new ChatRequest();
        request.setMessage("预约实验室需要什么步骤");

        mockMvc.perform(post("/api/v1/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.answer").exists())
                .andExpect(jsonPath("$.data.source").value("knowledge"));
    }

    @Test
    @DisplayName("知识库匹配测试：设备状态查询")
    @WithMockUser(username = "testuser", roles = {"student"})
    void testChatWithKnowledgeBaseEquipmentStatus() throws Exception {
        ChatRequest request = new ChatRequest();
        request.setMessage("设备状态有哪些");

        mockMvc.perform(post("/api/v1/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.answer").exists())
                .andExpect(jsonPath("$.data.source").value("knowledge"));
    }

    @Test
    @DisplayName("API 调用测试：知识库未匹配的问题")
    @WithMockUser(username = "testuser", roles = {"student"})
    void testChatWithApiFallback() throws Exception {
        ChatRequest request = new ChatRequest();
        request.setMessage("今天天气怎么样？");

        mockMvc.perform(post("/api/v1/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.answer").exists())
                .andExpect(jsonPath("$.data.source").value("api"));

        // 验证日志已保存，来源为 api
        Optional<AiChatLog> log = chatLogRepository.findByUserIdAndLogDate(
                testUser.getId(), java.time.LocalDate.now())
                .stream()
                .filter(l -> l.getUserInput().equals("今天天气怎么样？"))
                .findFirst();
        assertTrue(log.isPresent());
        assertEquals("api", log.get().getSource());
    }

    @Test
    @DisplayName("API 调用测试：系统相关问题但不在知识库")
    @WithMockUser(username = "testuser", roles = {"student"})
    void testChatWithApiForSystemRelatedQuestion() throws Exception {
        ChatRequest request = new ChatRequest();
        request.setMessage("如何修改我的密码？");

        mockMvc.perform(post("/api/v1/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.answer").exists())
                .andExpect(jsonPath("$.data.source").value("api"));
    }

    @Test
    @DisplayName("参数校验测试：空消息")
    @WithMockUser(username = "testuser", roles = {"student"})
    void testChatWithEmptyMessage() throws Exception {
        ChatRequest request = new ChatRequest();
        request.setMessage("");

        mockMvc.perform(post("/api/v1/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("参数校验测试：null 消息")
    @WithMockUser(username = "testuser", roles = {"student"})
    void testChatWithNullMessage() throws Exception {
        ChatRequest request = new ChatRequest();
        request.setMessage(null);

        mockMvc.perform(post("/api/v1/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("权限测试：未登录用户")
    void testChatWithoutAuthentication() throws Exception {
        ChatRequest request = new ChatRequest();
        request.setMessage("如何借用设备");

        // Spring Security 在没有认证时会返回 403 (AccessDeniedException)
        // 而不是 401，因为请求通过了过滤器但被 @PreAuthorize 拒绝
        mockMvc.perform(post("/api/v1/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("日志记录测试：验证日志按日期保存")
    @WithMockUser(username = "testuser", roles = {"student"})
    void testChatLogSaved() throws Exception {
        ChatRequest request = new ChatRequest();
        request.setMessage("测试日志记录");

        mockMvc.perform(post("/api/v1/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // 验证日志已保存
        java.time.LocalDate today = java.time.LocalDate.now();
        var logs = chatLogRepository.findByUserIdAndLogDate(testUser.getId(), today);
        assertFalse(logs.isEmpty());
        
        Optional<AiChatLog> log = logs.stream()
                .filter(l -> l.getUserInput().equals("测试日志记录"))
                .findFirst();
        assertTrue(log.isPresent());
        assertNotNull(log.get().getAiOutput());
        assertNotNull(log.get().getSource());
        assertEquals(testUser.getId(), log.get().getUserId());
        assertEquals(today, log.get().getLogDate());
    }

    @Test
    @DisplayName("单轮对话测试：不维护上下文")
    @WithMockUser(username = "testuser", roles = {"student"})
    void testSingleTurnConversation() throws Exception {
        // 第一次对话
        ChatRequest request1 = new ChatRequest();
        request1.setMessage("如何借用设备");

        mockMvc.perform(post("/api/v1/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.source").value("knowledge"));

        // 第二次对话（应该独立处理，不依赖第一次）
        ChatRequest request2 = new ChatRequest();
        request2.setMessage("设备状态");

        mockMvc.perform(post("/api/v1/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.source").value("knowledge"));

        // 验证两次对话都独立记录
        var logs = chatLogRepository.findByUserIdAndLogDate(
                testUser.getId(), java.time.LocalDate.now());
        assertEquals(2, logs.size());
    }
}

