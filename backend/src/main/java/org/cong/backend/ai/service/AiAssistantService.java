package org.cong.backend.ai.service;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.models.QwenParam;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import org.cong.backend.ai.config.DashScopeConfig;
import org.cong.backend.ai.dto.ChatRequest;
import org.cong.backend.ai.dto.ChatResponse;
import org.cong.backend.ai.entity.AiChatLog;
import org.cong.backend.ai.repository.AiChatLogRepository;
import org.cong.backend.common.BusinessException;
import org.cong.backend.security.SecurityUtils;
import org.cong.backend.user.entity.User;
import org.cong.backend.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AI 智能助手服务
 * 实现单轮对话，优先使用知识库，兜底调用通义千问 API
 */
@Service
public class AiAssistantService {

    private static final Logger logger = LoggerFactory.getLogger(AiAssistantService.class);

    private static final String SOURCE_KNOWLEDGE = "knowledge";
    private static final String SOURCE_API = "api";

    // 知识库：问题-答案映射（可根据实际需求扩展为数据库存储）
    private static final Map<String, String> KNOWLEDGE_BASE = new HashMap<>();

    static {
        // 初始化知识库
        String borrowAnswer = "设备借用流程：1. 登录系统后进入设备借用页面；2. 选择需要借用的设备，填写借用数量和用途；3. 提交借用申请；4. 等待管理员审批；5. 审批通过后，到实验室确认借出；6. 使用完毕后，及时归还设备。";
        String reservationAnswer = "实验室预约流程：1. 登录系统后进入实验室预约页面；2. 选择实验室和预约日期时间；3. 填写预约用途；4. 提交预约申请；5. 等待管理员审批；6. 审批通过后，按时使用实验室。";
        String statusAnswer = "设备状态包括：待入库、已入库、使用中、维修中、报废。设备状态变更需要管理员或教师权限。";
        String viewEquipmentAnswer = "查看设备：1. 登录系统后进入设备管理页面；2. 可以按名称、型号、状态等条件筛选；3. 点击设备名称可查看详细信息，包括设备状态、所属实验室、借用记录等。";
        String roleAnswer = "系统用户角色包括：管理员（admin）- 拥有所有权限；教师（teacher）- 可以管理设备和实验室；学生（student）- 可以借用设备和预约实验室。";
        
        // 设备借用相关
        KNOWLEDGE_BASE.put("如何借用设备", borrowAnswer);
        KNOWLEDGE_BASE.put("设备借用", borrowAnswer);
        KNOWLEDGE_BASE.put("借用设备", borrowAnswer);
        KNOWLEDGE_BASE.put("设备借用流程", borrowAnswer);
        
        // 实验室预约相关
        KNOWLEDGE_BASE.put("如何预约实验室", reservationAnswer);
        KNOWLEDGE_BASE.put("实验室预约", reservationAnswer);
        KNOWLEDGE_BASE.put("预约实验室", reservationAnswer);
        KNOWLEDGE_BASE.put("实验室预约流程", reservationAnswer);
        KNOWLEDGE_BASE.put("预约实验室流程", reservationAnswer);
        
        // 设备状态相关
        KNOWLEDGE_BASE.put("设备状态", statusAnswer);
        KNOWLEDGE_BASE.put("设备状态有哪些", statusAnswer);
        KNOWLEDGE_BASE.put("设备状态包括", statusAnswer);
        
        // 查看设备相关
        KNOWLEDGE_BASE.put("如何查看设备", viewEquipmentAnswer);
        KNOWLEDGE_BASE.put("查看设备", viewEquipmentAnswer);
        KNOWLEDGE_BASE.put("设备查询", viewEquipmentAnswer);
        
        // 用户角色相关
        KNOWLEDGE_BASE.put("用户角色", roleAnswer);
        KNOWLEDGE_BASE.put("权限说明", roleAnswer);
        KNOWLEDGE_BASE.put("角色权限", roleAnswer);
        
        // 设备维修
        KNOWLEDGE_BASE.put("设备维修", 
            "设备维修流程：1. 发现设备故障后，在系统中提交维修申请；2. 填写故障描述和紧急程度；3. 等待管理员审批；4. 审批通过后，设备进入维修状态；5. 维修完成后，管理员更新维修结果。");
        
        // 设备报废
        KNOWLEDGE_BASE.put("设备报废", 
            "设备报废流程：1. 管理员或教师提交设备报废申请；2. 填写报废原因；3. 等待管理员审批；4. 审批通过后，设备状态变更为报废。");
    }

    private final DashScopeConfig dashScopeConfig;
    private final AiChatLogRepository chatLogRepository;
    private final UserRepository userRepository;

    public AiAssistantService(DashScopeConfig dashScopeConfig,
                              AiChatLogRepository chatLogRepository,
                              UserRepository userRepository) {
        this.dashScopeConfig = dashScopeConfig;
        this.chatLogRepository = chatLogRepository;
        this.userRepository = userRepository;
    }

    /**
     * 处理用户消息，返回 AI 回答
     * 优先匹配知识库，未匹配则调用通义千问 API
     */
    @Transactional
    public ChatResponse chat(ChatRequest request) {
        String userInput = request.getMessage();
        if (!StringUtils.hasText(userInput)) {
            throw BusinessException.badRequest("输入内容不能为空");
        }

        Long userId = resolveCurrentUserId();
        if (userId == null) {
            throw BusinessException.unauthorized("未登录或用户不存在");
        }

        String answer;
        String source;

        // 优先：尝试从知识库匹配
        String knowledgeAnswer = matchKnowledgeBase(userInput);
        if (knowledgeAnswer != null) {
            answer = knowledgeAnswer;
            source = SOURCE_KNOWLEDGE;
        } else {
            // 兜底：调用通义千问 API
            answer = callDashScopeApi(userInput);
            source = SOURCE_API;
        }

        // 记录对话日志
        saveChatLog(userId, userInput, answer, source);

        return new ChatResponse(answer, source);
    }

    /**
     * 从知识库匹配答案
     * 使用关键词匹配和相似度匹配
     */
    private String matchKnowledgeBase(String userInput) {
        if (userInput == null || userInput.trim().isEmpty()) {
            return null;
        }

        String normalizedInput = userInput.trim().toLowerCase();

        // 1. 完全匹配
        for (Map.Entry<String, String> entry : KNOWLEDGE_BASE.entrySet()) {
            if (normalizedInput.equals(entry.getKey().toLowerCase())) {
                return entry.getValue();
            }
        }

        // 2. 包含匹配（高相似度）- 改进：检查用户输入是否包含知识库关键词
        for (Map.Entry<String, String> entry : KNOWLEDGE_BASE.entrySet()) {
            String key = entry.getKey().toLowerCase();
            // 如果用户输入包含知识库关键词，或者知识库关键词包含用户输入的核心部分
            if (normalizedInput.contains(key)) {
                return entry.getValue();
            }
            // 如果知识库关键词包含用户输入的核心部分（至少3个字符）
            if (normalizedInput.length() >= 3 && key.contains(normalizedInput)) {
                return entry.getValue();
            }
        }

        // 3. 关键词匹配（提取关键词进行匹配）- 改进：更智能的匹配
        List<String> keywords = extractKeywords(normalizedInput);
        // 按关键词长度降序排序，优先匹配长关键词
        keywords.sort((a, b) -> Integer.compare(b.length(), a.length()));
        
        for (String keyword : keywords) {
            if (keyword.length() < 2) {
                continue;
            }
            for (Map.Entry<String, String> entry : KNOWLEDGE_BASE.entrySet()) {
                String key = entry.getKey().toLowerCase();
                // 如果知识库关键词包含提取的关键词
                if (key.contains(keyword)) {
                    return entry.getValue();
                }
                // 如果提取的关键词包含知识库关键词的核心部分
                if (key.length() >= 2 && keyword.contains(key)) {
                    return entry.getValue();
                }
            }
        }

        return null;
    }

    /**
     * 提取关键词（简单实现）
     */
    private List<String> extractKeywords(String text) {
        // 常见停用词（注意：Set.of(...) 不允许重复元素，这里用 HashSet 容错）
        Set<String> stopWords = new HashSet<>(Arrays.asList(
                "的", "了", "在", "是", "我", "有", "和", "就",
                "不", "人", "都", "一", "一个", "上", "也", "很", "到", "说", "要", "去",
                "你", "会", "着", "没有", "看", "好", "自己", "这", "如何", "怎么", "什么",
                "需要", "哪些", "怎样", "步骤", "流程", "方法"
        ));
        
        List<String> keywords = new ArrayList<>();
        // 先尝试提取完整短语（2-4个字）
        for (int i = 0; i < text.length() - 1; i++) {
            for (int len = 4; len >= 2 && i + len <= text.length(); len--) {
                String phrase = text.substring(i, i + len);
                if (!stopWords.contains(phrase) && phrase.length() >= 2) {
                    keywords.add(phrase);
                }
            }
        }
        
        // 再提取单个词
        String[] words = text.split("[\\s，。、；：！？]+");
        for (String word : words) {
            word = word.trim();
            if (word.length() >= 2 && !stopWords.contains(word) && !keywords.contains(word)) {
                keywords.add(word);
            }
        }
        
        return keywords;
    }

    /**
     * 调用通义千问 API
     */
    private String callDashScopeApi(String userInput) {
        if (!StringUtils.hasText(dashScopeConfig.getApiKey())) {
            logger.warn("DashScope API Key 未配置，返回默认提示");
            return "抱歉，AI 服务暂时不可用，请联系管理员配置 API Key。";
        }

        try {
            Generation gen = new Generation();
            QwenParam param = QwenParam.builder()
                    .apiKey(dashScopeConfig.getApiKey())
                    .model(dashScopeConfig.getModel())
                    .messages(Collections.singletonList(
                            Message.builder()
                                    .role(Role.USER.getValue())
                                    .content(buildPrompt(userInput))
                                    .build()
                    ))
                    .resultFormat(QwenParam.ResultFormat.MESSAGE)
                    .build();

            // 调用 API，使用 var 让编译器推断实际返回类型
            var result = gen.call(param);
            
            // 通过反射安全地获取结果内容
            if (result != null) {
                String content = extractContentFromResult(result);
                if (content != null && !content.isEmpty()) {
                    return content;
                }
            }
            
            return "抱歉，AI 服务返回了空结果，请稍后重试。";
        } catch (ApiException | NoApiKeyException | InputRequiredException e) {
            logger.error("调用通义千问 API 失败", e);
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.length() > 100) {
                errorMsg = errorMsg.substring(0, 100) + "...";
            }
            return "抱歉，AI 服务调用失败，请稍后重试。如问题持续，请联系管理员。";
        } catch (Exception e) {
            logger.error("调用通义千问 API 发生未知错误", e);
            return "抱歉，AI 服务发生未知错误，请稍后重试。如问题持续，请联系管理员。";
        }
    }

    /**
     * 从 DashScope API 返回结果中提取内容
     * 使用反射以兼容不同版本的 SDK
     */
    private String extractContentFromResult(Object result) {
        try {
            // 获取 output
            Object output = invokeMethodOrGetField(result, "getOutput", "output");
            if (output == null) {
                return null;
            }
            
            // 获取 choices
            Object choices = invokeMethodOrGetField(output, "getChoices", "choices");
            if (!(choices instanceof List) || ((List<?>) choices).isEmpty()) {
                return null;
            }
            
            // 获取第一个 choice
            Object firstChoice = ((List<?>) choices).get(0);
            
            // 获取 message
            Object message = invokeMethodOrGetField(firstChoice, "getMessage", "message");
            if (message == null) {
                return null;
            }
            
            // 获取 content
            Object content = invokeMethodOrGetField(message, "getContent", "content");
            if (content != null) {
                return content.toString();
            }
        } catch (Exception e) {
            logger.warn("解析 DashScope API 响应时出错", e);
        }
        return null;
    }

    /**
     * 尝试通过方法或字段获取值
     */
    private Object invokeMethodOrGetField(Object obj, String methodName, String fieldName) {
        try {
            // 先尝试方法
            return obj.getClass().getMethod(methodName).invoke(obj);
        } catch (NoSuchMethodException e) {
            // 方法不存在，尝试字段
            try {
                var field = obj.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(obj);
            } catch (NoSuchFieldException | IllegalAccessException ex) {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 构建提示词
     * 限制 AI 的回答边界，明确这是为高校实验室设备管理系统提供问询的 AI
     */
    private String buildPrompt(String userInput) {
        return String.format(
            "你是一个专门为高校实验室设备管理系统提供问询服务的 AI 助手。\n" +
            "你的职责是回答用户关于实验室设备管理系统的咨询、解释和指引类问题。\n\n" +
            "重要限制：\n" +
            "1. 你只能回答与实验室设备管理系统相关的问题，包括：设备管理、设备借用、实验室预约、设备维修、设备报废、用户权限等。\n" +
            "2. 你不得生成或修改任何业务数据（如增删改查设备、预约、用户等）。\n" +
            "3. 你不得执行系统操作、生成 SQL、调用内部接口。\n" +
            "4. 你不得回答与系统无关的问题，如：政治、色情、暴力、违法内容、其他无关话题等。\n" +
            "5. 如果用户询问系统功能，请提供指引和说明，但不要执行实际操作。\n" +
            "6. 如果问题超出你的职责范围，请礼貌地告知用户，并引导用户咨询相关问题。\n\n" +
            "用户问题：%s\n\n" +
            "请根据以上规则回答用户的问题。",
            userInput
        );
    }

    /**
     * 保存对话日志
     */
    private void saveChatLog(Long userId, String userInput, String aiOutput, String source) {
        try {
            AiChatLog log = new AiChatLog();
            log.setUserId(userId);
            log.setUserInput(userInput != null ? userInput : "");
            log.setAiOutput(aiOutput != null ? aiOutput : "");
            log.setSource(source != null ? source : SOURCE_API);
            // 确保 logDate 被设置（通过 @PrePersist）
            chatLogRepository.saveAndFlush(log);
        } catch (Exception e) {
            logger.error("保存对话日志失败", e);
            // 日志保存失败不影响主流程，只记录错误
        }
    }

    /**
     * 解析当前用户 ID
     */
    private Long resolveCurrentUserId() {
        String username = SecurityUtils.getCurrentUsername();
        if (!StringUtils.hasText(username)) {
            return null;
        }
        return userRepository.findByUsername(username)
                .map(User::getId)
                .orElse(null);
    }
}

