package org.cong.backend.repair;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cong.backend.equipment.entity.Equipment;
import org.cong.backend.equipment.entity.EquipmentStatus;
import org.cong.backend.equipment.repository.EquipmentRepository;
import org.cong.backend.equipment.repository.EquipmentStatusRepository;
import org.cong.backend.laboratory.entity.Laboratory;
import org.cong.backend.laboratory.repository.LaboratoryRepository;
import org.cong.backend.repair.entity.EquipmentRepair;
import org.cong.backend.repair.repository.EquipmentRepairRepository;
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
class EquipmentRepairControllerTest {

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
    private EquipmentRepairRepository repairRepository;

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
                    lab.setName("维修模块测试实验室");
                    lab.setCode("LAB-REPAIR-" + UUID.randomUUID().toString().substring(0, 8));
                    lab.setLocation("测试楼-测试房间");
                    lab.setType("测试类型");
                    lab.setStatus(1);
                    lab.setDescription("用于维修模块测试创建的实验室");
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
                    status.setDescription("用于维修模块测试创建的设备状态");
                    return equipmentStatusRepository.save(status);
                });
    }

    private Equipment createTestEquipment() {
        Laboratory lab = pickAvailableLaboratory();
        EquipmentStatus status = pickInstoredStatus();
        Equipment eq = new Equipment();
        eq.setName("维修测试设备");
        eq.setModel("TEST-MODEL-REPAIR");
        eq.setSpecification("测试规格");
        eq.setAssetCode("EQ-REPAIR-" + UUID.randomUUID().toString().substring(0, 8));
        eq.setUnitPrice(1000.00);
        eq.setQuantity(1);
        eq.setSupplier("测试供应商");
        eq.setPurchaseDate(java.time.LocalDate.now());
        eq.setWarrantyPeriod(12);
        eq.setStatusId(status.getId());
        eq.setLaboratoryId(lab.getId());
        eq.setDescription("用于维修模块测试的设备");
        eq.setCreateTime(LocalDateTime.now());
        eq.setUpdateTime(LocalDateTime.now());
        return equipmentRepository.save(eq);
    }

    @Test
    @DisplayName("测试提交维修申请 - 学生角色")
    @WithMockUser(username = "student_repair", roles = {"student"})
    void testCreateRepair_Student() throws Exception {
        User student = ensureUser("student_repair", "student", "维修测试学生");
        Equipment equipment = createTestEquipment();

        String requestJson = String.format("""
                {
                    "equipmentId": %d,
                    "reportDate": "2024-01-15T10:00:00",
                    "faultDescription": "设备无法正常启动"
                }
                """, equipment.getId());

        MvcResult result = mockMvc.perform(post("/api/v1/repair")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("维修申请提交成功"))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.equipmentId").value(equipment.getId()))
                .andExpect(jsonPath("$.data.reporterId").value(student.getId()))
                .andExpect(jsonPath("$.data.status").value(0))
                .andReturn();

        // 验证数据库中的记录
        JsonNode responseNode = objectMapper.readTree(result.getResponse().getContentAsString());
        Long repairId = responseNode.get("data").get("id").asLong();
        EquipmentRepair saved = repairRepository.findById(repairId).orElseThrow();
        assert saved.getEquipmentId().equals(equipment.getId());
        assert saved.getReporterId().equals(student.getId());
        assert saved.getStatus() == 0;
    }

    @Test
    @DisplayName("测试获取维修记录列表 - 管理员")
    @WithMockUser(username = "admin_repair", roles = {"admin"})
    void testGetRepairList_Admin() throws Exception {
        User admin = ensureUser("admin_repair", "admin", "维修测试管理员");
        Equipment equipment = createTestEquipment();

        // 创建测试数据
        EquipmentRepair repair = new EquipmentRepair();
        repair.setEquipmentId(equipment.getId());
        repair.setReporterId(admin.getId());
        repair.setReportDate(LocalDateTime.now());
        repair.setFaultDescription("测试故障描述");
        repair.setStatus(0);
        repairRepository.save(repair);

        mockMvc.perform(get("/api/v1/repair")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.total").exists());
    }

    @Test
    @DisplayName("测试获取维修记录详情 - 学生只能查看自己的")
    @WithMockUser(username = "student_repair2", roles = {"student"})
    void testGetRepairDetail_StudentOwn() throws Exception {
        User student = ensureUser("student_repair2", "student", "维修测试学生2");
        Equipment equipment = createTestEquipment();

        EquipmentRepair repair = new EquipmentRepair();
        repair.setEquipmentId(equipment.getId());
        repair.setReporterId(student.getId());
        repair.setReportDate(LocalDateTime.now());
        repair.setFaultDescription("测试故障描述");
        repair.setStatus(0);
        EquipmentRepair saved = repairRepository.save(repair);

        mockMvc.perform(get("/api/v1/repair/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(saved.getId()));
    }

    @Test
    @DisplayName("测试获取维修记录详情 - 学生不能查看他人的")
    @WithMockUser(username = "student_repair3", roles = {"student"})
    void testGetRepairDetail_StudentOther() throws Exception {
        User student1 = ensureUser("student_repair_other1", "student", "学生1");
        User student2 = ensureUser("student_repair3", "student", "学生2");
        Equipment equipment = createTestEquipment();

        EquipmentRepair repair = new EquipmentRepair();
        repair.setEquipmentId(equipment.getId());
        repair.setReporterId(student1.getId());
        repair.setReportDate(LocalDateTime.now());
        repair.setFaultDescription("测试故障描述");
        repair.setStatus(0);
        EquipmentRepair saved = repairRepository.save(repair);

        mockMvc.perform(get("/api/v1/repair/" + saved.getId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("无权限查看该维修记录"));
    }

    @Test
    @DisplayName("测试更新维修状态 - 管理员")
    @WithMockUser(username = "admin_repair2", roles = {"admin"})
    void testUpdateRepairStatus_Admin() throws Exception {
        User admin = ensureUser("admin_repair2", "admin", "维修测试管理员2");
        Equipment equipment = createTestEquipment();

        EquipmentRepair repair = new EquipmentRepair();
        repair.setEquipmentId(equipment.getId());
        repair.setReporterId(admin.getId());
        repair.setReportDate(LocalDateTime.now());
        repair.setFaultDescription("测试故障描述");
        repair.setStatus(0);
        EquipmentRepair saved = repairRepository.save(repair);

        String requestJson = """
                {
                    "status": 2,
                    "repairResult": "已更换故障部件，设备恢复正常",
                    "repairDate": "2024-01-20T15:00:00"
                }
                """;

        mockMvc.perform(put("/api/v1/repair/" + saved.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value(2))
                .andExpect(jsonPath("$.data.repairResult").exists());
    }

    @Test
    @DisplayName("测试更新维修状态 - 学生无权限")
    @WithMockUser(username = "student_repair4", roles = {"student"})
    void testUpdateRepairStatus_Student() throws Exception {
        User student = ensureUser("student_repair4", "student", "维修测试学生4");
        Equipment equipment = createTestEquipment();

        EquipmentRepair repair = new EquipmentRepair();
        repair.setEquipmentId(equipment.getId());
        repair.setReporterId(student.getId());
        repair.setReportDate(LocalDateTime.now());
        repair.setFaultDescription("测试故障描述");
        repair.setStatus(0);
        EquipmentRepair saved = repairRepository.save(repair);

        String requestJson = """
                {
                    "status": 1
                }
                """;

        mockMvc.perform(put("/api/v1/repair/" + saved.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("测试获取维修统计 - 管理员")
    @WithMockUser(username = "admin_repair3", roles = {"admin"})
    void testGetRepairStats_Admin() throws Exception {
        User admin = ensureUser("admin_repair3", "admin", "维修测试管理员3");
        Equipment equipment = createTestEquipment();

        // 创建不同状态的维修记录
        for (int i = 0; i < 3; i++) {
            EquipmentRepair repair = new EquipmentRepair();
            repair.setEquipmentId(equipment.getId());
            repair.setReporterId(admin.getId());
            repair.setReportDate(LocalDateTime.now());
            repair.setFaultDescription("测试故障描述" + i);
            repair.setStatus(i);
            repairRepository.save(repair);
        }

        mockMvc.perform(get("/api/v1/repair/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalCount").exists())
                .andExpect(jsonPath("$.data.pendingCount").exists())
                .andExpect(jsonPath("$.data.repairRate").exists());
    }

    @Test
    @DisplayName("测试获取维修统计 - 学生无权限")
    @WithMockUser(username = "student_repair5", roles = {"student"})
    void testGetRepairStats_Student() throws Exception {
        mockMvc.perform(get("/api/v1/repair/stats"))
                .andExpect(status().isForbidden());
    }
}

