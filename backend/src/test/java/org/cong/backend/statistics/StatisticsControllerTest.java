package org.cong.backend.statistics;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cong.backend.borrow.entity.EquipmentBorrow;
import org.cong.backend.borrow.repository.EquipmentBorrowRepository;
import org.cong.backend.equipment.entity.Equipment;
import org.cong.backend.equipment.entity.EquipmentStatus;
import org.cong.backend.equipment.repository.EquipmentRepository;
import org.cong.backend.equipment.repository.EquipmentStatusRepository;
import org.cong.backend.laboratory.entity.Laboratory;
import org.cong.backend.laboratory.repository.LaboratoryRepository;
import org.cong.backend.reservation.entity.LaboratoryReservation;
import org.cong.backend.reservation.repository.LaboratoryReservationRepository;
import org.cong.backend.user.entity.User;
import org.cong.backend.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class StatisticsControllerTest {

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
    private EquipmentBorrowRepository borrowRepository;

    @Autowired
    private LaboratoryReservationRepository reservationRepository;

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

    private Laboratory createLaboratory() {
        Laboratory lab = new Laboratory();
        lab.setName("统计测试实验室");
        lab.setCode("LAB-STAT-" + UUID.randomUUID().toString().substring(0, 8));
        lab.setLocation("测试楼-统计房间");
        lab.setType("测试类型");
        lab.setStatus(1);
        lab.setDescription("用于统计模块测试创建的实验室");
        lab.setCreateTime(LocalDateTime.now());
        lab.setUpdateTime(LocalDateTime.now());
        return laboratoryRepository.save(lab);
    }

    private EquipmentStatus pickInstoredStatus() {
        return equipmentStatusRepository.findByCode("instored")
                .orElseGet(() -> {
                    EquipmentStatus status = new EquipmentStatus();
                    status.setName("已入库");
                    status.setCode("instored");
                    status.setDescription("用于统计模块测试创建的设备状态");
                    return equipmentStatusRepository.save(status);
                });
    }

    private Equipment createEquipment(Laboratory lab, String name) {
        EquipmentStatus status = pickInstoredStatus();
        Equipment eq = new Equipment();
        eq.setName(name);
        eq.setModel("型号-" + UUID.randomUUID().toString().substring(0, 8));
        eq.setSpecification("规格说明");
        eq.setAssetCode("ASSET-" + UUID.randomUUID().toString().substring(0, 8));
        eq.setUnitPrice(1000.0);
        eq.setQuantity(5);
        eq.setSupplier("供应商");
        eq.setPurchaseDate(LocalDate.now().minusMonths(1));
        eq.setWarrantyPeriod(12);
        eq.setStatusId(status.getId());
        eq.setLaboratoryId(lab.getId());
        eq.setDescription("用于统计模块测试的设备");
        eq.setCreateTime(LocalDateTime.now());
        eq.setUpdateTime(LocalDateTime.now());
        return equipmentRepository.save(eq);
    }

    @Test
    @DisplayName("获取设备使用次数统计")
    void getEquipmentUsageStats() throws Exception {
        ensureUser("admin_stat", "admin", "统计测试管理员");
        
        Laboratory lab = createLaboratory();
        Equipment eq1 = createEquipment(lab, "设备1");
        Equipment eq2 = createEquipment(lab, "设备2");
        
        User student = ensureUser("student_stat", "student", "统计测试学生");
        
        // 创建一些已归还的借用记录
        EquipmentBorrow borrow1 = new EquipmentBorrow();
        borrow1.setEquipmentId(eq1.getId());
        borrow1.setUserId(student.getId());
        borrow1.setBorrowDate(LocalDateTime.now().minusDays(10));
        borrow1.setPlanReturnDate(LocalDateTime.now().minusDays(5));
        borrow1.setActualReturnDate(LocalDateTime.now().minusDays(5));
        borrow1.setPurpose("测试用途1");
        borrow1.setQuantity(1);
        borrow1.setStatus(4); // 已归还
        borrowRepository.save(borrow1);
        
        EquipmentBorrow borrow2 = new EquipmentBorrow();
        borrow2.setEquipmentId(eq1.getId());
        borrow2.setUserId(student.getId());
        borrow2.setBorrowDate(LocalDateTime.now().minusDays(8));
        borrow2.setPlanReturnDate(LocalDateTime.now().minusDays(3));
        borrow2.setActualReturnDate(LocalDateTime.now().minusDays(3));
        borrow2.setPurpose("测试用途2");
        borrow2.setQuantity(1);
        borrow2.setStatus(4); // 已归还
        borrowRepository.save(borrow2);
        
        EquipmentBorrow borrow3 = new EquipmentBorrow();
        borrow3.setEquipmentId(eq2.getId());
        borrow3.setUserId(student.getId());
        borrow3.setBorrowDate(LocalDateTime.now().minusDays(6));
        borrow3.setPlanReturnDate(LocalDateTime.now().minusDays(1));
        borrow3.setActualReturnDate(LocalDateTime.now().minusDays(1));
        borrow3.setPurpose("测试用途3");
        borrow3.setQuantity(1);
        borrow3.setStatus(4); // 已归还
        borrowRepository.save(borrow3);
        
        // 调用接口
        MvcResult result = mockMvc.perform(get("/api/v1/statistics/equipment-usage")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin_stat").roles("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.equipmentList").isArray())
                .andExpect(jsonPath("$.data.totalEquipmentCount").isNumber())
                .andExpect(jsonPath("$.data.totalUsageCount").isNumber())
                .andReturn();
        
        // 验证设备1的使用次数为2，设备2的使用次数为1
        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
        JsonNode equipmentList = data.path("equipmentList");
        boolean foundEq1 = false;
        boolean foundEq2 = false;
        for (JsonNode item : equipmentList) {
            if (item.path("equipmentId").asLong() == eq1.getId()) {
                foundEq1 = true;
                assertEquals(2, item.path("usageCount").asLong(), "设备1的使用次数应为2");
            }
            if (item.path("equipmentId").asLong() == eq2.getId()) {
                foundEq2 = true;
                assertEquals(1, item.path("usageCount").asLong(), "设备2的使用次数应为1");
            }
        }
        assertTrue(foundEq1, "应找到设备1的统计信息");
        assertTrue(foundEq2, "应找到设备2的统计信息");
        
        // 验证总使用次数至少包含我们创建的3条记录
        long totalUsageCount = data.path("totalUsageCount").asLong();
        assertTrue(totalUsageCount >= 3, "总使用次数应至少为3（包含我们创建的记录）");
    }

    @Test
    @DisplayName("获取借用/逾期统计")
    void getBorrowStats() throws Exception {
        ensureUser("admin_stat2", "admin", "统计测试管理员2");
        
        Laboratory lab = createLaboratory();
        Equipment eq = createEquipment(lab, "统计测试设备");
        User student = ensureUser("student_stat2", "student", "统计测试学生2");
        
        // 创建不同状态的借用记录
        EquipmentBorrow borrow1 = new EquipmentBorrow();
        borrow1.setEquipmentId(eq.getId());
        borrow1.setUserId(student.getId());
        borrow1.setBorrowDate(LocalDateTime.now());
        borrow1.setPlanReturnDate(LocalDateTime.now().plusDays(7));
        borrow1.setPurpose("待审批");
        borrow1.setQuantity(1);
        borrow1.setStatus(0); // 待审批
        borrowRepository.save(borrow1);
        
        EquipmentBorrow borrow2 = new EquipmentBorrow();
        borrow2.setEquipmentId(eq.getId());
        borrow2.setUserId(student.getId());
        borrow2.setBorrowDate(LocalDateTime.now());
        borrow2.setPlanReturnDate(LocalDateTime.now().plusDays(7));
        borrow2.setPurpose("已通过");
        borrow2.setQuantity(1);
        borrow2.setStatus(1); // 已通过
        borrowRepository.save(borrow2);
        
        EquipmentBorrow borrow3 = new EquipmentBorrow();
        borrow3.setEquipmentId(eq.getId());
        borrow3.setUserId(student.getId());
        borrow3.setBorrowDate(LocalDateTime.now());
        borrow3.setPlanReturnDate(LocalDateTime.now().plusDays(7));
        borrow3.setPurpose("已借出");
        borrow3.setQuantity(1);
        borrow3.setStatus(3); // 已借出
        borrowRepository.save(borrow3);
        
        EquipmentBorrow borrow4 = new EquipmentBorrow();
        borrow4.setEquipmentId(eq.getId());
        borrow4.setUserId(student.getId());
        borrow4.setBorrowDate(LocalDateTime.now().minusDays(10));
        borrow4.setPlanReturnDate(LocalDateTime.now().minusDays(3));
        borrow4.setActualReturnDate(LocalDateTime.now().minusDays(3));
        borrow4.setPurpose("已归还");
        borrow4.setQuantity(1);
        borrow4.setStatus(4); // 已归还
        borrowRepository.save(borrow4);
        
        EquipmentBorrow borrow5 = new EquipmentBorrow();
        borrow5.setEquipmentId(eq.getId());
        borrow5.setUserId(student.getId());
        borrow5.setBorrowDate(LocalDateTime.now().minusDays(10));
        borrow5.setPlanReturnDate(LocalDateTime.now().minusDays(1));
        borrow5.setPurpose("已逾期");
        borrow5.setQuantity(1);
        borrow5.setStatus(5); // 已逾期
        borrowRepository.save(borrow5);
        
        EquipmentBorrow borrow6 = new EquipmentBorrow();
        borrow6.setEquipmentId(eq.getId());
        borrow6.setUserId(student.getId());
        borrow6.setBorrowDate(LocalDateTime.now());
        borrow6.setPlanReturnDate(LocalDateTime.now().plusDays(7));
        borrow6.setPurpose("已拒绝");
        borrow6.setQuantity(1);
        borrow6.setStatus(2); // 已拒绝
        borrowRepository.save(borrow6);
        
        // 调用接口
        MvcResult result = mockMvc.perform(get("/api/v1/statistics/borrow-stats")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin_stat2").roles("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalBorrowCount").isNumber())
                .andExpect(jsonPath("$.data.pendingCount").isNumber())
                .andExpect(jsonPath("$.data.approvedCount").isNumber())
                .andExpect(jsonPath("$.data.borrowedCount").isNumber())
                .andExpect(jsonPath("$.data.returnedCount").isNumber())
                .andExpect(jsonPath("$.data.overdueCount").isNumber())
                .andExpect(jsonPath("$.data.rejectedCount").isNumber())
                .andReturn();
        
        // 验证统计结果至少包含我们创建的记录
        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
        long totalBorrowCount = data.path("totalBorrowCount").asLong();
        long pendingCount = data.path("pendingCount").asLong();
        long approvedCount = data.path("approvedCount").asLong();
        long borrowedCount = data.path("borrowedCount").asLong();
        long returnedCount = data.path("returnedCount").asLong();
        long overdueCount = data.path("overdueCount").asLong();
        long rejectedCount = data.path("rejectedCount").asLong();
        
        // 验证总数至少包含我们创建的6条记录
        assertTrue(totalBorrowCount >= 6, "总借用记录数应至少为6（包含我们创建的记录）");
        // 验证各状态统计至少包含我们创建的记录
        assertTrue(pendingCount >= 1, "待审批数量应至少为1（包含我们创建的记录）");
        assertTrue(approvedCount >= 1, "已通过数量应至少为1（包含我们创建的记录）");
        assertTrue(borrowedCount >= 1, "已借出数量应至少为1（包含我们创建的记录）");
        assertTrue(returnedCount >= 1, "已归还数量应至少为1（包含我们创建的记录）");
        assertTrue(overdueCount >= 1, "已逾期数量应至少为1（包含我们创建的记录）");
        assertTrue(rejectedCount >= 1, "已拒绝数量应至少为1（包含我们创建的记录）");
        
        // 验证各状态统计之和不超过总数
        long sum = pendingCount + approvedCount + borrowedCount + returnedCount + overdueCount + rejectedCount;
        assertTrue(sum <= totalBorrowCount, "各状态统计之和不应超过总记录数");
    }

    @Test
    @DisplayName("获取到期提醒")
    void getReminders() throws Exception {
        User student = ensureUser("student_stat3", "student", "统计测试学生3");
        
        Laboratory lab = createLaboratory();
        Equipment eq = createEquipment(lab, "提醒测试设备");
        
        // 创建即将到期的借用记录（已借出，计划归还日期在明天）
        EquipmentBorrow borrow1 = new EquipmentBorrow();
        borrow1.setEquipmentId(eq.getId());
        borrow1.setUserId(student.getId());
        borrow1.setBorrowDate(LocalDateTime.now().minusDays(5));
        borrow1.setPlanReturnDate(LocalDateTime.now().plusDays(1)); // 明天到期
        borrow1.setPurpose("即将到期");
        borrow1.setQuantity(1);
        borrow1.setStatus(3); // 已借出
        borrowRepository.save(borrow1);
        
        // 创建已逾期的借用记录
        EquipmentBorrow borrow2 = new EquipmentBorrow();
        borrow2.setEquipmentId(eq.getId());
        borrow2.setUserId(student.getId());
        borrow2.setBorrowDate(LocalDateTime.now().minusDays(10));
        borrow2.setPlanReturnDate(LocalDateTime.now().minusDays(1)); // 昨天到期
        borrow2.setPurpose("已逾期");
        borrow2.setQuantity(1);
        borrow2.setStatus(5); // 已逾期
        borrowRepository.save(borrow2);
        
        // 创建即将到期的预约记录（已通过，预约日期在明天）
        LaboratoryReservation reservation1 = new LaboratoryReservation();
        reservation1.setLaboratoryId(lab.getId());
        reservation1.setUserId(student.getId());
        reservation1.setReserveDate(LocalDate.now().plusDays(1)); // 明天
        reservation1.setStartTime(LocalTime.of(9, 0));
        reservation1.setEndTime(LocalTime.of(11, 0));
        reservation1.setPurpose("即将到期预约");
        reservation1.setStatus(1); // 已通过
        reservationRepository.save(reservation1);
        
        // 创建已过期的预约记录
        LaboratoryReservation reservation2 = new LaboratoryReservation();
        reservation2.setLaboratoryId(lab.getId());
        reservation2.setUserId(student.getId());
        reservation2.setReserveDate(LocalDate.now().minusDays(1)); // 昨天
        reservation2.setStartTime(LocalTime.of(9, 0));
        reservation2.setEndTime(LocalTime.of(11, 0));
        reservation2.setPurpose("已过期预约");
        reservation2.setStatus(1); // 已通过
        reservationRepository.save(reservation2);
        
        // 调用接口
        MvcResult result = mockMvc.perform(get("/api/v1/statistics/reminders")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("student_stat3").roles("student")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.hasReminder").value(true))
                .andExpect(jsonPath("$.data.reminderCount").isNumber())
                .andExpect(jsonPath("$.data.borrowReminders").isArray())
                .andExpect(jsonPath("$.data.reservationReminders").isArray())
                .andReturn();
        
        // 验证提醒内容
        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
        assertTrue(data.path("reminderCount").asInt() >= 4, "提醒数量应至少为4");
        assertTrue(data.path("borrowReminders").size() >= 2, "借用提醒应至少为2");
        assertTrue(data.path("reservationReminders").size() >= 2, "预约提醒应至少为2");
    }

    @Test
    @DisplayName("获取到期提醒 - 无提醒情况")
    void getReminders_NoReminders() throws Exception {
        User student = ensureUser("student_stat4", "student", "统计测试学生4");
        
        // 调用接口，应该返回无提醒
        mockMvc.perform(get("/api/v1/statistics/reminders")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("student_stat4").roles("student")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.hasReminder").value(false))
                .andExpect(jsonPath("$.data.reminderCount").value(0))
                .andExpect(jsonPath("$.data.borrowReminders").isArray())
                .andExpect(jsonPath("$.data.reservationReminders").isArray());
    }

    @Test
    @DisplayName("权限测试 - 学生不能访问统计接口")
    void testPermission_StudentCannotAccessStats() throws Exception {
        ensureUser("student_stat5", "student", "统计测试学生5");
        
        // 学生不能访问设备使用统计
        mockMvc.perform(get("/api/v1/statistics/equipment-usage")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("student_stat5").roles("student")))
                .andExpect(status().isForbidden());
        
        // 学生不能访问借用统计
        mockMvc.perform(get("/api/v1/statistics/borrow-stats")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("student_stat5").roles("student")))
                .andExpect(status().isForbidden());
        
        // 学生可以访问提醒接口
        mockMvc.perform(get("/api/v1/statistics/reminders")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("student_stat5").roles("student")))
                .andExpect(status().isOk());
    }
}

