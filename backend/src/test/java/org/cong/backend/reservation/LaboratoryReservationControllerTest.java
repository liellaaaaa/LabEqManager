package org.cong.backend.reservation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.time.LocalTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class LaboratoryReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LaboratoryRepository laboratoryRepository;

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

    private Laboratory createAvailableLaboratory() {
        Laboratory lab = new Laboratory();
        lab.setName("预约测试实验室");
        lab.setCode("LAB-RESERVE-" + UUID.randomUUID().toString().substring(0, 8));
        lab.setLocation("测试楼-测试房间");
        lab.setType("测试类型");
        lab.setStatus(1); // 可用
        lab.setDescription("用于预约模块测试创建的实验室");
        lab.setCreateTime(LocalDateTime.now());
        lab.setUpdateTime(LocalDateTime.now());
        return laboratoryRepository.save(lab);
    }

    private long createReservationAsStudent(String username, long laboratoryId, 
                                           LocalDate reserveDate, LocalTime startTime, LocalTime endTime) throws Exception {
        String body = """
                {
                  "laboratoryId": %d,
                  "reserveDate": "%s",
                  "startTime": "%s",
                  "endTime": "%s",
                  "purpose": "课程实验"
                }
                """.formatted(laboratoryId, reserveDate.toString(), startTime.toString(), endTime.toString());

        MvcResult result = mockMvc.perform(post("/api/v1/reservation")
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
    @DisplayName("正常预约成功：申请->审批->完成 主流程闭环")
    @WithMockUser(username = "student1", roles = "student")
    void reservationApproveCompleteFlow() throws Exception {
        ensureUser("student1", "student", "测试学生1");
        ensureUser("admin1", "admin", "测试管理员1");

        Laboratory lab = createAvailableLaboratory();
        LocalDate reserveDate = LocalDate.now().plusDays(1);
        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(11, 0);

        long reservationId = createReservationAsStudent("student1", lab.getId(), reserveDate, startTime, endTime);

        // 学生查看自己的预约详情
        mockMvc.perform(get("/api/v1/reservation/{id}", reservationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(reservationId))
                .andExpect(jsonPath("$.data.status").value(0));

        // 管理员审批通过
        String approveBody = """
                {"status":1,"remark":"审批通过"}
                """;
        mockMvc.perform(put("/api/v1/reservation/{id}/approve", reservationId)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin1").roles("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(approveBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(1))
                .andExpect(jsonPath("$.data.approveRemark").value("审批通过"));

        // 管理员标记完成
        String completeBody = """
                {
                  "actualStartTime": "09:10:00",
                  "actualEndTime": "10:50:00",
                  "usageRemark": "实验顺利完成"
                }
                """;
        mockMvc.perform(put("/api/v1/reservation/{id}/complete", reservationId)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin1").roles("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(completeBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(4))
                .andExpect(jsonPath("$.data.actualStartTime").value("09:10:00"))
                .andExpect(jsonPath("$.data.actualEndTime").value("10:50:00"));
    }

    @Test
    @DisplayName("时间重叠冲突检测：应该失败")
    @WithMockUser(username = "student2", roles = "student")
    void timeConflictTest() throws Exception {
        ensureUser("student2", "student", "测试学生2");
        ensureUser("admin2", "admin", "测试管理员2");

        Laboratory lab = createAvailableLaboratory();
        LocalDate reserveDate = LocalDate.now().plusDays(1);
        LocalTime startTime1 = LocalTime.of(9, 0);
        LocalTime endTime1 = LocalTime.of(11, 0);
        LocalTime startTime2 = LocalTime.of(10, 0); // 与第一个预约重叠
        LocalTime endTime2 = LocalTime.of(12, 0);

        // 创建第一个预约
        long reservationId1 = createReservationAsStudent("student2", lab.getId(), reserveDate, startTime1, endTime1);

        // 审批通过第一个预约
        mockMvc.perform(put("/api/v1/reservation/{id}/approve", reservationId1)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin2").roles("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":1}"))
                .andExpect(status().isOk());

        // 尝试创建重叠时间的预约，应该失败
        String conflictBody = """
                {
                  "laboratoryId": %d,
                  "reserveDate": "%s",
                  "startTime": "%s",
                  "endTime": "%s",
                  "purpose": "冲突测试"
                }
                """.formatted(lab.getId(), reserveDate.toString(), startTime2.toString(), endTime2.toString());

        mockMvc.perform(post("/api/v1/reservation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(conflictBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("预约冲突")));
    }

    @Test
    @DisplayName("审批流转：待审批->已通过->已拒绝")
    @WithMockUser(username = "student3", roles = "student")
    void approvalFlowTest() throws Exception {
        ensureUser("student3", "student", "测试学生3");
        ensureUser("admin3", "admin", "测试管理员3");

        Laboratory lab = createAvailableLaboratory();
        LocalDate reserveDate = LocalDate.now().plusDays(1);
        LocalTime startTime = LocalTime.of(14, 0);
        LocalTime endTime = LocalTime.of(16, 0);

        long reservationId = createReservationAsStudent("student3", lab.getId(), reserveDate, startTime, endTime);

        // 管理员审批通过
        mockMvc.perform(put("/api/v1/reservation/{id}/approve", reservationId)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin3").roles("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":1,\"remark\":\"审批通过\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(1));

        // 验证状态已更新
        mockMvc.perform(get("/api/v1/reservation/{id}", reservationId)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin3").roles("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(1))
                .andExpect(jsonPath("$.data.statusName").value("已通过"));

        // 创建另一个预约用于测试拒绝
        long reservationId2 = createReservationAsStudent("student3", lab.getId(), 
                reserveDate.plusDays(1), startTime, endTime);

        // 管理员审批拒绝
        mockMvc.perform(put("/api/v1/reservation/{id}/approve", reservationId2)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin3").roles("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":2,\"remark\":\"审批拒绝：实验室维护中\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(2))
                .andExpect(jsonPath("$.data.approveRemark").value("审批拒绝：实验室维护中"));
    }

    @Test
    @DisplayName("取消预约：学生和管理员都可以取消")
    @WithMockUser(username = "student4", roles = "student")
    void cancelReservationTest() throws Exception {
        ensureUser("student4", "student", "测试学生4");
        ensureUser("admin4", "admin", "测试管理员4");

        Laboratory lab = createAvailableLaboratory();
        LocalDate reserveDate = LocalDate.now().plusDays(1);
        LocalTime startTime = LocalTime.of(15, 0);
        LocalTime endTime = LocalTime.of(17, 0);

        // 创建预约
        long reservationId = createReservationAsStudent("student4", lab.getId(), reserveDate, startTime, endTime);

        // 学生取消自己的预约
        String cancelBody = """
                {"remark": "因课程调整取消预约"}
                """;
        mockMvc.perform(put("/api/v1/reservation/{id}/cancel", reservationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cancelBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(3))
                .andExpect(jsonPath("$.data.approveRemark").value(org.hamcrest.Matchers.containsString("取消备注")));

        // 创建另一个预约，管理员取消
        long reservationId2 = createReservationAsStudent("student4", lab.getId(), 
                reserveDate.plusDays(1), startTime, endTime);

        // 管理员取消
        mockMvc.perform(put("/api/v1/reservation/{id}/cancel", reservationId2)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin4").roles("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"remark\":\"管理员取消\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(3));
    }

    @Test
    @DisplayName("状态异常测试：已通过的不能再审批")
    @WithMockUser(username = "student5", roles = "student")
    void statusExceptionTest() throws Exception {
        ensureUser("student5", "student", "测试学生5");
        ensureUser("admin5", "admin", "测试管理员5");

        Laboratory lab = createAvailableLaboratory();
        LocalDate reserveDate = LocalDate.now().plusDays(1);
        LocalTime startTime = LocalTime.of(10, 0);
        LocalTime endTime = LocalTime.of(12, 0);

        long reservationId = createReservationAsStudent("student5", lab.getId(), reserveDate, startTime, endTime);

        // 第一次审批通过
        mockMvc.perform(put("/api/v1/reservation/{id}/approve", reservationId)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin5").roles("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(1));

        // 尝试再次审批，应该失败
        mockMvc.perform(put("/api/v1/reservation/{id}/approve", reservationId)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin5").roles("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("已审批过")));

        // 已取消的预约不能再取消
        long reservationId2 = createReservationAsStudent("student5", lab.getId(), 
                reserveDate.plusDays(1), startTime, endTime);

        // 先取消
        mockMvc.perform(put("/api/v1/reservation/{id}/cancel", reservationId2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(3));

        // 已完成的预约不能再标记为完成
        long reservationId3 = createReservationAsStudent("student5", lab.getId(), 
                reserveDate.plusDays(2), startTime, endTime);

        // 审批通过
        mockMvc.perform(put("/api/v1/reservation/{id}/approve", reservationId3)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin5").roles("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":1}"))
                .andExpect(status().isOk());

        // 标记完成
        mockMvc.perform(put("/api/v1/reservation/{id}/complete", reservationId3)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin5").roles("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(4));

        // 再次尝试标记完成，应该失败
        mockMvc.perform(put("/api/v1/reservation/{id}/complete", reservationId3)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin5").roles("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("检查冲突接口：正确检测时间冲突")
    @WithMockUser(username = "student6", roles = "student")
    void checkConflictTest() throws Exception {
        ensureUser("student6", "student", "测试学生6");
        ensureUser("admin6", "admin", "测试管理员6");

        Laboratory lab = createAvailableLaboratory();
        LocalDate reserveDate = LocalDate.now().plusDays(1);
        LocalTime startTime1 = LocalTime.of(9, 0);
        LocalTime endTime1 = LocalTime.of(11, 0);

        // 创建并审批通过一个预约
        long reservationId1 = createReservationAsStudent("student6", lab.getId(), reserveDate, startTime1, endTime1);
        mockMvc.perform(put("/api/v1/reservation/{id}/approve", reservationId1)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin6").roles("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":1}"))
                .andExpect(status().isOk());

        // 检查冲突：重叠时间段
        String checkConflictBody = """
                {
                  "laboratoryId": %d,
                  "reserveDate": "%s",
                  "startTime": "%s",
                  "endTime": "%s"
                }
                """.formatted(lab.getId(), reserveDate.toString(), 
                        LocalTime.of(10, 0).toString(), LocalTime.of(12, 0).toString());

        mockMvc.perform(post("/api/v1/reservation/check-conflict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(checkConflictBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hasConflict").value(true))
                .andExpect(jsonPath("$.data.conflictList").isArray())
                .andExpect(jsonPath("$.data.conflictList[0].id").value(reservationId1));

        // 检查冲突：不重叠时间段
        String noConflictBody = """
                {
                  "laboratoryId": %d,
                  "reserveDate": "%s",
                  "startTime": "%s",
                  "endTime": "%s"
                }
                """.formatted(lab.getId(), reserveDate.toString(), 
                        LocalTime.of(14, 0).toString(), LocalTime.of(16, 0).toString());

        mockMvc.perform(post("/api/v1/reservation/check-conflict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(noConflictBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hasConflict").value(false));
    }

    @Test
    @DisplayName("获取可用时间段接口")
    @WithMockUser(username = "student7", roles = "student")
    void getAvailableTimeTest() throws Exception {
        ensureUser("student7", "student", "测试学生7");
        ensureUser("admin7", "admin", "测试管理员7");

        Laboratory lab = createAvailableLaboratory();
        LocalDate reserveDate = LocalDate.now().plusDays(1);

        // 创建并审批通过几个预约
        long reservationId1 = createReservationAsStudent("student7", lab.getId(), 
                reserveDate, LocalTime.of(9, 0), LocalTime.of(11, 0));
        long reservationId2 = createReservationAsStudent("student7", lab.getId(), 
                reserveDate, LocalTime.of(14, 0), LocalTime.of(16, 0));

        mockMvc.perform(put("/api/v1/reservation/{id}/approve", reservationId1)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin7").roles("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":1}"))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/v1/reservation/{id}/approve", reservationId2)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin7").roles("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":1}"))
                .andExpect(status().isOk());

        // 获取可用时间段
        mockMvc.perform(get("/api/v1/reservation/available-time")
                        .param("laboratoryId", String.valueOf(lab.getId()))
                        .param("reserveDate", reserveDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.availableTimeSlots").isArray());
    }

    @Test
    @DisplayName("权限测试：学生只能查看自己的预约记录")
    @WithMockUser(username = "student8", roles = "student")
    void permissionTest() throws Exception {
        ensureUser("student8", "student", "测试学生8");
        ensureUser("student9", "student", "测试学生9");
        ensureUser("admin8", "admin", "测试管理员8");

        Laboratory lab = createAvailableLaboratory();
        LocalDate reserveDate = LocalDate.now().plusDays(1);
        LocalTime startTime = LocalTime.of(10, 0);
        LocalTime endTime = LocalTime.of(12, 0);

        // 学生8创建预约
        long reservationIdByStudent8 = createReservationAsStudent("student8", lab.getId(), reserveDate, startTime, endTime);

        // 学生9创建预约
        long reservationIdByStudent9;
        {
            String body = """
                    {
                      "laboratoryId": %d,
                      "reserveDate": "%s",
                      "startTime": "%s",
                      "endTime": "%s",
                      "purpose": "测试用途"
                    }
                    """.formatted(lab.getId(), reserveDate.toString(), startTime.toString(), endTime.toString());

            MvcResult created = mockMvc.perform(post("/api/v1/reservation")
                            .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("student9").roles("student"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andReturn();
            reservationIdByStudent9 = objectMapper.readTree(created.getResponse().getContentAsString()).path("data").path("id").asLong();
        }

        // 学生8应该能查看自己的预约记录
        mockMvc.perform(get("/api/v1/reservation/{id}", reservationIdByStudent8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 学生9不应该能查看学生8的预约记录
        mockMvc.perform(get("/api/v1/reservation/{id}", reservationIdByStudent8)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("student9").roles("student")))
                .andExpect(status().isForbidden());

        // 管理员应该能查看所有预约记录
        mockMvc.perform(get("/api/v1/reservation/{id}", reservationIdByStudent8)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin8").roles("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}

