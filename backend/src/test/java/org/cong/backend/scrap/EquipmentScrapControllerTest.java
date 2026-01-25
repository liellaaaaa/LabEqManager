package org.cong.backend.scrap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cong.backend.equipment.entity.Equipment;
import org.cong.backend.equipment.entity.EquipmentStatus;
import org.cong.backend.equipment.repository.EquipmentRepository;
import org.cong.backend.equipment.repository.EquipmentStatusRepository;
import org.cong.backend.laboratory.entity.Laboratory;
import org.cong.backend.laboratory.repository.LaboratoryRepository;
import org.cong.backend.scrap.entity.EquipmentScrap;
import org.cong.backend.scrap.repository.EquipmentScrapRepository;
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

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EquipmentScrapControllerTest {

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

    @Autowired
    private EquipmentScrapRepository scrapRepository;

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
                    lab.setName("报废模块测试实验室");
                    lab.setCode("LAB-SCRAP-" + UUID.randomUUID().toString().substring(0, 8));
                    lab.setLocation("测试楼-测试房间");
                    lab.setType("测试类型");
                    lab.setStatus(1);
                    lab.setDescription("用于报废模块测试创建的实验室");
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
                    status.setDescription("用于报废模块测试创建的设备状态");
                    return equipmentStatusRepository.save(status);
                });
    }

    private Equipment createTestEquipment() {
        Laboratory lab = pickAvailableLaboratory();
        EquipmentStatus status = pickInstoredStatus();
        Equipment eq = new Equipment();
        eq.setName("报废测试设备");
        eq.setModel("TEST-MODEL-SCRAP");
        eq.setSpecification("测试规格");
        eq.setAssetCode("EQ-SCRAP-" + UUID.randomUUID().toString().substring(0, 8));
        eq.setUnitPrice(1000.00);
        eq.setQuantity(1);
        eq.setSupplier("测试供应商");
        eq.setPurchaseDate(java.time.LocalDate.now());
        eq.setWarrantyPeriod(12);
        eq.setStatusId(status.getId());
        eq.setLaboratoryId(lab.getId());
        eq.setDescription("用于报废模块测试的设备");
        eq.setCreateTime(LocalDateTime.now());
        eq.setUpdateTime(LocalDateTime.now());
        return equipmentRepository.save(eq);
    }

    @Test
    @DisplayName("测试提交报废申请 - 教师角色")
    @WithMockUser(username = "teacher_scrap", roles = {"teacher"})
    void testCreateScrap_Teacher() throws Exception {
        User teacher = ensureUser("teacher_scrap", "teacher", "报废测试教师");
        Equipment equipment = createTestEquipment();

        String requestJson = String.format("""
                {
                    "equipmentId": %d,
                    "applyDate": "2024-01-15T10:00:00",
                    "scrapReason": "设备老化严重，无法满足教学需求"
                }
                """, equipment.getId());

        MvcResult result = mockMvc.perform(post("/api/v1/scrap")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("报废申请提交成功"))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.equipmentId").value(equipment.getId()))
                .andExpect(jsonPath("$.data.applicantId").value(teacher.getId()))
                .andExpect(jsonPath("$.data.status").value(0))
                .andReturn();

        // 验证数据库中的记录
        JsonNode responseNode = objectMapper.readTree(result.getResponse().getContentAsString());
        Long scrapId = responseNode.get("data").get("id").asLong();
        EquipmentScrap saved = scrapRepository.findById(scrapId).orElseThrow();
        assert saved.getEquipmentId().equals(equipment.getId());
        assert saved.getApplicantId().equals(teacher.getId());
        assert saved.getStatus() == 0;
    }

    @Test
    @DisplayName("测试获取报废记录列表 - 管理员")
    @WithMockUser(username = "admin_scrap", roles = {"admin"})
    void testGetScrapList_Admin() throws Exception {
        User admin = ensureUser("admin_scrap", "admin", "报废测试管理员");
        Equipment equipment = createTestEquipment();

        // 创建测试数据
        EquipmentScrap scrap = new EquipmentScrap();
        scrap.setEquipmentId(equipment.getId());
        scrap.setApplicantId(admin.getId());
        scrap.setApplyDate(LocalDateTime.now());
        scrap.setScrapReason("测试报废原因");
        scrap.setStatus(0);
        scrapRepository.save(scrap);

        mockMvc.perform(get("/api/v1/scrap")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.total").exists());
    }

    @Test
    @DisplayName("测试获取报废记录详情 - 学生只能查看自己的")
    @WithMockUser(username = "student_scrap2", roles = {"student"})
    void testGetScrapDetail_StudentOwn() throws Exception {
        User student = ensureUser("student_scrap2", "student", "报废测试学生2");
        Equipment equipment = createTestEquipment();

        EquipmentScrap scrap = new EquipmentScrap();
        scrap.setEquipmentId(equipment.getId());
        scrap.setApplicantId(student.getId());
        scrap.setApplyDate(LocalDateTime.now());
        scrap.setScrapReason("测试报废原因");
        scrap.setStatus(0);
        EquipmentScrap saved = scrapRepository.save(scrap);

        mockMvc.perform(get("/api/v1/scrap/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(saved.getId()));
    }

    @Test
    @DisplayName("测试获取报废记录详情 - 学生不能查看他人的")
    @WithMockUser(username = "student_scrap3", roles = {"student"})
    void testGetScrapDetail_StudentOther() throws Exception {
        User student1 = ensureUser("student_scrap_other1", "student", "学生1");
        User student2 = ensureUser("student_scrap3", "student", "学生2");
        Equipment equipment = createTestEquipment();

        EquipmentScrap scrap = new EquipmentScrap();
        scrap.setEquipmentId(equipment.getId());
        scrap.setApplicantId(student1.getId());
        scrap.setApplyDate(LocalDateTime.now());
        scrap.setScrapReason("测试报废原因");
        scrap.setStatus(0);
        EquipmentScrap saved = scrapRepository.save(scrap);

        mockMvc.perform(get("/api/v1/scrap/" + saved.getId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("无权限查看该报废记录"));
    }

    @Test
    @DisplayName("测试审批报废申请 - 管理员通过")
    @WithMockUser(username = "admin_scrap2", roles = {"admin"})
    void testApproveScrap_Admin_Approved() throws Exception {
        User admin = ensureUser("admin_scrap2", "admin", "报废测试管理员2");
        Equipment equipment = createTestEquipment();

        EquipmentScrap scrap = new EquipmentScrap();
        scrap.setEquipmentId(equipment.getId());
        scrap.setApplicantId(admin.getId());
        scrap.setApplyDate(LocalDateTime.now());
        scrap.setScrapReason("测试报废原因");
        scrap.setStatus(0);
        EquipmentScrap saved = scrapRepository.save(scrap);

        String requestJson = """
                {
                    "status": 1,
                    "remark": "审批通过，设备可进行报废处理"
                }
                """;

        mockMvc.perform(put("/api/v1/scrap/" + saved.getId() + "/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value(1))
                .andExpect(jsonPath("$.data.approverId").value(admin.getId()))
                .andExpect(jsonPath("$.data.approveRemark").exists());
    }

    @Test
    @DisplayName("测试审批报废申请 - 管理员拒绝")
    @WithMockUser(username = "admin_scrap3", roles = {"admin"})
    void testApproveScrap_Admin_Rejected() throws Exception {
        User admin = ensureUser("admin_scrap3", "admin", "报废测试管理员3");
        Equipment equipment = createTestEquipment();

        EquipmentScrap scrap = new EquipmentScrap();
        scrap.setEquipmentId(equipment.getId());
        scrap.setApplicantId(admin.getId());
        scrap.setApplyDate(LocalDateTime.now());
        scrap.setScrapReason("测试报废原因");
        scrap.setStatus(0);
        EquipmentScrap saved = scrapRepository.save(scrap);

        String requestJson = """
                {
                    "status": 2,
                    "remark": "设备仍可使用，暂不批准报废"
                }
                """;

        mockMvc.perform(put("/api/v1/scrap/" + saved.getId() + "/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value(2));
    }

    @Test
    @DisplayName("测试审批报废申请 - 学生无权限")
    @WithMockUser(username = "student_scrap4", roles = {"student"})
    void testApproveScrap_Student() throws Exception {
        User student = ensureUser("student_scrap4", "student", "报废测试学生4");
        Equipment equipment = createTestEquipment();

        EquipmentScrap scrap = new EquipmentScrap();
        scrap.setEquipmentId(equipment.getId());
        scrap.setApplicantId(student.getId());
        scrap.setApplyDate(LocalDateTime.now());
        scrap.setScrapReason("测试报废原因");
        scrap.setStatus(0);
        EquipmentScrap saved = scrapRepository.save(scrap);

        String requestJson = """
                {
                    "status": 1
                }
                """;

        mockMvc.perform(put("/api/v1/scrap/" + saved.getId() + "/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("测试重复审批 - 应该失败")
    @WithMockUser(username = "admin_scrap4", roles = {"admin"})
    void testApproveScrap_Duplicate() throws Exception {
        User admin = ensureUser("admin_scrap4", "admin", "报废测试管理员4");
        Equipment equipment = createTestEquipment();

        EquipmentScrap scrap = new EquipmentScrap();
        scrap.setEquipmentId(equipment.getId());
        scrap.setApplicantId(admin.getId());
        scrap.setApplyDate(LocalDateTime.now());
        scrap.setScrapReason("测试报废原因");
        scrap.setStatus(1); // 已审批
        EquipmentScrap saved = scrapRepository.save(scrap);

        String requestJson = """
                {
                    "status": 1
                }
                """;

        mockMvc.perform(put("/api/v1/scrap/" + saved.getId() + "/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("该报废申请已审批过，无法重复审批"));
    }

    @Test
    @DisplayName("测试获取报废统计 - 管理员")
    @WithMockUser(username = "admin_scrap5", roles = {"admin"})
    void testGetScrapStats_Admin() throws Exception {
        User admin = ensureUser("admin_scrap5", "admin", "报废测试管理员5");
        Equipment equipment = createTestEquipment();

        // 创建不同状态的报废记录
        for (int i = 0; i < 3; i++) {
            EquipmentScrap scrap = new EquipmentScrap();
            scrap.setEquipmentId(equipment.getId());
            scrap.setApplicantId(admin.getId());
            scrap.setApplyDate(LocalDateTime.now());
            scrap.setScrapReason("测试报废原因" + i);
            scrap.setStatus(i);
            scrapRepository.save(scrap);
        }

        mockMvc.perform(get("/api/v1/scrap/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalCount").exists())
                .andExpect(jsonPath("$.data.pendingCount").exists())
                .andExpect(jsonPath("$.data.approvalRate").exists());
    }

    @Test
    @DisplayName("测试获取报废统计 - 学生无权限")
    @WithMockUser(username = "student_scrap5", roles = {"student"})
    void testGetScrapStats_Student() throws Exception {
        mockMvc.perform(get("/api/v1/scrap/stats"))
                .andExpect(status().isForbidden());
    }
}

