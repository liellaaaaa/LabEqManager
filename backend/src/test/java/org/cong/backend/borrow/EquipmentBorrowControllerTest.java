package org.cong.backend.borrow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cong.backend.equipment.entity.Equipment;
import org.cong.backend.equipment.entity.EquipmentStatus;
import org.cong.backend.equipment.repository.EquipmentRepository;
import org.cong.backend.equipment.repository.EquipmentStatusRepository;
import org.cong.backend.laboratory.entity.Laboratory;
import org.cong.backend.laboratory.repository.LaboratoryRepository;
import org.cong.backend.user.entity.User;
import org.cong.backend.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EquipmentBorrowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LaboratoryRepository laboratoryRepository;

    @Autowired
    private EquipmentStatusRepository equipmentStatusRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    private User ensureUser(String username, String roleCode, String name) {
        return userRepository.findByUsername(username).orElseGet(() -> {
            User u = new User();
            u.setUsername(username);
            u.setPassword("pwd");
            u.setName(name);
            u.setDepartment("测试院系");
            u.setRoleCode(roleCode);
            u.setStatus(1);
            u.setCreateTime(LocalDateTime.now());
            u.setUpdateTime(LocalDateTime.now());
            return userRepository.save(u);
        });
    }

    private Laboratory pickAvailableLaboratory() {
        return laboratoryRepository.findAll().stream()
                .filter(lab -> lab.getStatus() != null && lab.getStatus() == 1)
                .findFirst()
                .orElseGet(() -> {
                    Laboratory lab = new Laboratory();
                    lab.setName("借用模块测试实验室");
                    lab.setCode("LAB-BORROW-" + UUID.randomUUID().toString().substring(0, 8));
                    lab.setLocation("测试楼-测试房间");
                    lab.setType("测试类型");
                    lab.setStatus(1);
                    lab.setDescription("用于借用模块测试创建的实验室");
                    lab.setCreateTime(LocalDateTime.now());
                    lab.setUpdateTime(LocalDateTime.now());
                    return laboratoryRepository.save(lab);
                });
    }

    private EquipmentStatus pickInstoredStatus() {
        return equipmentStatusRepository.findByCode("instored")
                .orElseGet(() -> {
                    EquipmentStatus status = new EquipmentStatus();
                    status.setName("已入库");
                    status.setCode("instored");
                    status.setDescription("用于借用模块测试创建的设备状态");
                    return equipmentStatusRepository.save(status);
                });
    }

    private Equipment createBorrowableEquipment(int quantity) {
        Laboratory lab = pickAvailableLaboratory();
        EquipmentStatus status = pickInstoredStatus();
        Equipment eq = new Equipment();
        eq.setName("借用测试设备");
        eq.setModel("BORROW-MODEL");
        eq.setSpecification("BORROW-SPEC");
        eq.setAssetCode("BORROW-ASSET-" + UUID.randomUUID());
        eq.setUnitPrice(100.0);
        eq.setQuantity(quantity);
        eq.setSupplier("测试供应商");
        eq.setPurchaseDate(LocalDate.now());
        eq.setWarrantyPeriod(12);
        eq.setStatusId(status.getId());
        eq.setLaboratoryId(lab.getId());
        eq.setDescription("用于借用模块测试的设备");
        eq.setCreateTime(LocalDateTime.now());
        eq.setUpdateTime(LocalDateTime.now());
        return equipmentRepository.save(eq);
    }

    private long createBorrowAsStudent(String username, long equipmentId, int quantity, LocalDateTime borrowDate, LocalDateTime planReturnDate) throws Exception {
        String body = """
                {
                  "equipmentId": %d,
                  "borrowDate": "%s",
                  "planReturnDate": "%s",
                  "purpose": "课程设计",
                  "quantity": %d
                }
                """.formatted(equipmentId, borrowDate.toString(), planReturnDate.toString(), quantity);

        MvcResult result = mockMvc.perform(post("/api/v1/borrow")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value(0))
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.path("data").path("id").asLong();
    }

    @Test
    @DisplayName("申请->审批->借出->归还 主流程闭环")
    @WithMockUser(username = "borrower1", roles = "student")
    void borrowApproveBorrowReturnFlow() throws Exception {
        ensureUser("borrower1", "student", "测试学生");
        ensureUser("admin1", "admin", "测试管理员");

        Equipment eq = createBorrowableEquipment(2);
        LocalDateTime borrowDate = LocalDateTime.now().plusHours(1);
        LocalDateTime planReturnDate = borrowDate.plusDays(3);
        long borrowId = createBorrowAsStudent("borrower1", eq.getId(), 1, borrowDate, planReturnDate);

        // 学生查看自己的借用详情
        mockMvc.perform(get("/api/v1/borrow/{id}", borrowId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(borrowId));

        // 管理员审批通过
        String approveBody = """
                {"status":1,"remark":"审批通过"}
                """;
        mockMvc.perform(put("/api/v1/borrow/{id}/approve", borrowId)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin1").roles("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(approveBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(1));

        // 管理员确认借出
        mockMvc.perform(put("/api/v1/borrow/{id}/borrow", borrowId)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin1").roles("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(3));

        // 可用数量：总2，已借出1，可用1
        mockMvc.perform(get("/api/v1/borrow/available-quantity/{equipmentId}", eq.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalQuantity").value(2))
                .andExpect(jsonPath("$.data.borrowedQuantity").value(1))
                .andExpect(jsonPath("$.data.availableQuantity").value(1));

        // 学生归还
        String returnBody = """
                {"remark":"设备完好"}
                """;
        mockMvc.perform(put("/api/v1/borrow/{id}/return", borrowId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(returnBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(4))
                .andExpect(jsonPath("$.data.actualReturnDate").isNotEmpty());
    }

    @Test
    @DisplayName("管理员标记逾期：已借出且超过计划归还日期 -> 已逾期")
    void markOverdue() throws Exception {
        ensureUser("admin2", "admin", "测试管理员2");
        User borrower = ensureUser("borrower2", "student", "测试学生2");
        Equipment eq = createBorrowableEquipment(1);

        // 直接造一条“已借出”的记录：通过调用接口流程保证字段完整
        long borrowId;
        {
            // 先用借用人身份创建申请
            MvcResult created = mockMvc.perform(post("/api/v1/borrow")
                            .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("borrower2").roles("student"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "equipmentId": %d,
                                      "borrowDate": "%s",
                                      "planReturnDate": "%s",
                                      "purpose": "测试逾期",
                                      "quantity": 1
                                    }
                                    """.formatted(eq.getId(), LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(1))))
                    .andExpect(status().isOk())
                    .andReturn();
            borrowId = objectMapper.readTree(created.getResponse().getContentAsString()).path("data").path("id").asLong();
        }

        // 审批通过
        mockMvc.perform(put("/api/v1/borrow/{id}/approve", borrowId)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin2").roles("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":1}"))
                .andExpect(status().isOk());

        // 确认借出（此时 planReturnDate 已经过去）
        mockMvc.perform(put("/api/v1/borrow/{id}/borrow", borrowId)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin2").roles("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(3));

        // 标记逾期
        mockMvc.perform(put("/api/v1/borrow/mark-overdue")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin2").roles("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.overdueCount").isNumber());

        // 再查详情应为逾期（5）或已归还（若外部数据影响不应发生；这里按 5 断言）
        mockMvc.perform(get("/api/v1/borrow/{id}", borrowId)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin2").roles("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(5));
    }

    @Test
    @DisplayName("获取借用列表：支持分页、筛选和排序")
    void getBorrowList() throws Exception {
        ensureUser("admin3", "admin", "测试管理员3");
        ensureUser("borrower3", "student", "测试学生3");
        ensureUser("borrower4", "student", "测试学生4");

        // 创建两个设备用于测试
        Equipment eq1 = createBorrowableEquipment(5);
        Equipment eq2 = createBorrowableEquipment(5);

        // 创建多个借用记录
        long borrowId1, borrowId2, borrowId3;

        // 第一条借用记录：学生3借用设备1
        {
            MvcResult created = mockMvc.perform(post("/api/v1/borrow")
                            .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("borrower3").roles("student"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "equipmentId": %d,
                                      "borrowDate": "%s",
                                      "planReturnDate": "%s",
                                      "purpose": "测试用途1",
                                      "quantity": 2
                                    }
                                    """.formatted(eq1.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(4))))
                    .andExpect(status().isOk())
                    .andReturn();
            borrowId1 = objectMapper.readTree(created.getResponse().getContentAsString()).path("data").path("id").asLong();
        }

        // 第二条借用记录：学生4借用设备2
        {
            MvcResult created = mockMvc.perform(post("/api/v1/borrow")
                            .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("borrower4").roles("student"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "equipmentId": %d,
                                      "borrowDate": "%s",
                                      "planReturnDate": "%s",
                                      "purpose": "测试用途2",
                                      "quantity": 1
                                    }
                                    """.formatted(eq2.getId(), LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(5))))
                    .andExpect(status().isOk())
                    .andReturn();
            borrowId2 = objectMapper.readTree(created.getResponse().getContentAsString()).path("data").path("id").asLong();
        }

        // 第三条借用记录：学生3借用设备2
        {
            MvcResult created = mockMvc.perform(post("/api/v1/borrow")
                            .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("borrower3").roles("student"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "equipmentId": %d,
                                      "borrowDate": "%s",
                                      "planReturnDate": "%s",
                                      "purpose": "测试用途3",
                                      "quantity": 1
                                    }
                                    """.formatted(eq2.getId(), LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(6))))
                    .andExpect(status().isOk())
                    .andReturn();
            borrowId3 = objectMapper.readTree(created.getResponse().getContentAsString()).path("data").path("id").asLong();
        }

        // 管理员审批通过第一条和第二条记录
        mockMvc.perform(put("/api/v1/borrow/{id}/approve", borrowId1)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin3").roles("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":1}"))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/v1/borrow/{id}/approve", borrowId2)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin3").roles("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":1}"))
                .andExpect(status().isOk());

        // 确认借出第一条记录
        mockMvc.perform(put("/api/v1/borrow/{id}/borrow", borrowId1)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin3").roles("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());

        // 测试1：获取所有借用记录（分页）
        mockMvc.perform(get("/api/v1/borrow")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin3").roles("admin"))
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.total").isNumber());

        // 测试2：按设备ID筛选
        mockMvc.perform(get("/api/v1/borrow")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin3").roles("admin"))
                        .param("equipmentId", String.valueOf(eq1.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 测试3：按状态筛选（已借出状态为3）
        mockMvc.perform(get("/api/v1/borrow")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin3").roles("admin"))
                        .param("status", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 测试4：排序
        mockMvc.perform(get("/api/v1/borrow")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin3").roles("admin"))
                        .param("sortBy", "createTime")
                        .param("sortOrder", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("申请借用数量超过设备可用数量：应该失败")
    void createBorrowWithExceedQuantity() throws Exception {
        ensureUser("admin4", "admin", "测试管理员4");
        ensureUser("borrower5", "student", "测试学生5");
        ensureUser("borrower6", "student", "测试学生6");

        // 创建一个数量为2的设备
        Equipment eq = createBorrowableEquipment(2);

        // 第一个学生借用2个，将设备数量用完
        long borrowId1;
        {
            MvcResult created = mockMvc.perform(post("/api/v1/borrow")
                            .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("borrower5").roles("student"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "equipmentId": %d,
                                      "borrowDate": "%s",
                                      "planReturnDate": "%s",
                                      "purpose": "测试用途1",
                                      "quantity": 2
                                    }
                                    """.formatted(eq.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(4))))
                    .andExpect(status().isOk())
                    .andReturn();
            borrowId1 = objectMapper.readTree(created.getResponse().getContentAsString()).path("data").path("id").asLong();
        }

        // 管理员审批通过
        mockMvc.perform(put("/api/v1/borrow/{id}/approve", borrowId1)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin4").roles("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":1}"))
                .andExpect(status().isOk());

        // 确认借出
        mockMvc.perform(put("/api/v1/borrow/{id}/borrow", borrowId1)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin4").roles("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());

        // 检查可用数量：应该为0
        mockMvc.perform(get("/api/v1/borrow/available-quantity/{equipmentId}", eq.getId())
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin4").roles("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.availableQuantity").value(0));

        // 第二个学生尝试借用该设备，应该失败
        mockMvc.perform(post("/api/v1/borrow")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("borrower6").roles("student"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "equipmentId": %d,
                                  "borrowDate": "%s",
                                  "planReturnDate": "%s",
                                  "purpose": "测试用途2",
                                  "quantity": 1
                                }
                                """.formatted(eq.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(4))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("管理员审批拒绝：应该将状态改为已拒绝")
    void approveBorrowWithReject() throws Exception {
        ensureUser("admin5", "admin", "测试管理员5");
        ensureUser("borrower7", "student", "测试学生7");

        // 创建一个设备
        Equipment eq = createBorrowableEquipment(1);

        // 创建借用申请
        long borrowId;
        {
            MvcResult created = mockMvc.perform(post("/api/v1/borrow")
                            .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("borrower7").roles("student"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "equipmentId": %d,
                                      "borrowDate": "%s",
                                      "planReturnDate": "%s",
                                      "purpose": "测试用途",
                                      "quantity": 1
                                    }
                                    """.formatted(eq.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(4))))
                    .andExpect(status().isOk())
                    .andReturn();
            borrowId = objectMapper.readTree(created.getResponse().getContentAsString()).path("data").path("id").asLong();
        }

        // 管理员审批拒绝
        mockMvc.perform(put("/api/v1/borrow/{id}/approve", borrowId)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin5").roles("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":2,\"remark\":\"审批拒绝：设备暂不可用\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(2));

        // 检查借用记录状态：应该为已拒绝
        mockMvc.perform(get("/api/v1/borrow/{id}", borrowId)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin5").roles("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(2))
                .andExpect(jsonPath("$.data.approveRemark").value("审批拒绝：设备暂不可用"));
    }

    @Test
    @DisplayName("权限测试：学生只能查看自己的借用记录")
    void permissionTest() throws Exception {
        ensureUser("admin6", "admin", "测试管理员6");
        ensureUser("borrower8", "student", "测试学生8");
        ensureUser("borrower9", "student", "测试学生9");

        // 创建一个设备
        Equipment eq = createBorrowableEquipment(2);

        // 学生8创建借用申请
        long borrowIdByStudent8;
        {
            MvcResult created = mockMvc.perform(post("/api/v1/borrow")
                            .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("borrower8").roles("student"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "equipmentId": %d,
                                      "borrowDate": "%s",
                                      "planReturnDate": "%s",
                                      "purpose": "测试用途",
                                      "quantity": 1
                                    }
                                    """.formatted(eq.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(4))))
                    .andExpect(status().isOk())
                    .andReturn();
            borrowIdByStudent8 = objectMapper.readTree(created.getResponse().getContentAsString()).path("data").path("id").asLong();
        }

        // 学生9创建借用申请
        long borrowIdByStudent9;
        {
            MvcResult created = mockMvc.perform(post("/api/v1/borrow")
                            .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("borrower9").roles("student"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "equipmentId": %d,
                                      "borrowDate": "%s",
                                      "planReturnDate": "%s",
                                      "purpose": "测试用途",
                                      "quantity": 1
                                    }
                                    """.formatted(eq.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(4))))
                    .andExpect(status().isOk())
                    .andReturn();
            borrowIdByStudent9 = objectMapper.readTree(created.getResponse().getContentAsString()).path("data").path("id").asLong();
        }

        // 测试1：学生8应该能查看自己的借用记录
        mockMvc.perform(get("/api/v1/borrow/{id}", borrowIdByStudent8)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("borrower8").roles("student")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 测试2：学生9应该能查看自己的借用记录
        mockMvc.perform(get("/api/v1/borrow/{id}", borrowIdByStudent9)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("borrower9").roles("student")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 测试3：学生9不应该能查看学生8的借用记录（应该返回403或404）
        // 注意：后端实现为明确拒绝策略，这里期望 403
        mockMvc.perform(get("/api/v1/borrow/{id}", borrowIdByStudent8)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("borrower9").roles("student")))
                .andExpect(status().isForbidden());

        // 测试4：学生8不应该能查看学生9的借用记录（应该返回403或404）
        mockMvc.perform(get("/api/v1/borrow/{id}", borrowIdByStudent9)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("borrower8").roles("student")))
                .andExpect(status().isForbidden());

        // 测试5：管理员应该能查看所有借用记录
        mockMvc.perform(get("/api/v1/borrow/{id}", borrowIdByStudent8)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin6").roles("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/v1/borrow/{id}", borrowIdByStudent9)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin6").roles("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("无效参数测试：各种错误参数场景")
    void invalidParametersTest() throws Exception {
        ensureUser("admin7", "admin", "测试管理员7");
        ensureUser("borrower10", "student", "测试学生10");

        // 创建一个设备
        Equipment eq = createBorrowableEquipment(1);

        // 测试1：借用数量为负数
        mockMvc.perform(post("/api/v1/borrow")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("borrower10").roles("student"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "equipmentId": %d,
                                  "borrowDate": "%s",
                                  "planReturnDate": "%s",
                                  "purpose": "测试用途",
                                  "quantity": -1
                                }
                                """.formatted(eq.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(4))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.quantity").value(1));

        // 测试2：借用数量为0
        mockMvc.perform(post("/api/v1/borrow")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("borrower10").roles("student"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "equipmentId": %d,
                                  "borrowDate": "%s",
                                  "planReturnDate": "%s",
                                  "purpose": "测试用途",
                                  "quantity": 0
                                }
                                """.formatted(eq.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(4))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.quantity").value(1));

        // 测试3：借用日期在计划归还日期之后
        mockMvc.perform(post("/api/v1/borrow")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("borrower10").roles("student"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "equipmentId": %d,
                                  "borrowDate": "%s",
                                  "planReturnDate": "%s",
                                  "purpose": "测试用途",
                                  "quantity": 1
                                }
                                """.formatted(eq.getId(), LocalDateTime.now().plusDays(5), LocalDateTime.now().plusDays(1))))
                .andExpect(status().isBadRequest());

        // 测试4：设备ID不存在
        mockMvc.perform(post("/api/v1/borrow")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("borrower10").roles("student"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "equipmentId": 999999,
                                  "borrowDate": "%s",
                                  "planReturnDate": "%s",
                                  "purpose": "测试用途",
                                  "quantity": 1
                                }
                                """.formatted(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(4))))
                .andExpect(status().isNotFound());

        // 测试5：用途为空
        mockMvc.perform(post("/api/v1/borrow")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("borrower10").roles("student"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "equipmentId": %d,
                                  "borrowDate": "%s",
                                  "planReturnDate": "%s",
                                  "purpose": "",
                                  "quantity": 1
                                }
                                """.formatted(eq.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(4))))
                .andExpect(status().isOk());
    }
}


