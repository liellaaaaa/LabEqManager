package org.cong.backend.laboratory;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cong.backend.laboratory.dto.CreateLaboratoryRequest;
import org.cong.backend.laboratory.dto.UpdateLaboratoryRequest;
import org.cong.backend.laboratory.dto.UpdateStatusRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class LaboratoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Long createdLabId;

    @BeforeEach
    @WithMockUser(roles = "admin")
    void setUp() throws Exception {
        if (createdLabId == null) {
            CreateLaboratoryRequest request = new CreateLaboratoryRequest();
            request.setName("测试实验室");
            // 使用随机实验室编号，避免与数据库中已有数据冲突
            request.setCode("LAB-" + UUID.randomUUID());
            request.setLocation("A-101");
            request.setArea(50.0);
            request.setCapacity(30);
            request.setType("普通实验室");
            request.setStatus(1);
            request.setManagerId(null);
            request.setDescription("用于测试的实验室");

            MvcResult result = mockMvc.perform(post("/api/v1/laboratory")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andReturn();

            String content = result.getResponse().getContentAsString();
            // 简单从返回 JSON 中提取 id（ApiResponse.data.id）
            Long id = objectMapper.readTree(content).path("data").path("id").asLong();
            createdLabId = id != 0 ? id : null;
        }
    }

    @Test
    @DisplayName("GET /api/v1/laboratory - 实验室列表查询")
    @WithMockUser(roles = {"admin", "teacher", "student"})
    void getLaboratoryList() throws Exception {
        mockMvc.perform(get("/api/v1/laboratory")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/laboratory/{id} - 实验室详情查询")
    @WithMockUser(roles = {"admin", "teacher", "student"})
    void getLaboratoryById() throws Exception {
        assertThat(createdLabId).isNotNull();

        mockMvc.perform(get("/api/v1/laboratory/{id}", createdLabId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(createdLabId));
    }

    @Test
    @DisplayName("POST /api/v1/laboratory - 创建实验室")
    @WithMockUser(roles = "admin")
    void createLaboratory() throws Exception {
        CreateLaboratoryRequest request = new CreateLaboratoryRequest();
        request.setName("测试实验室2");
        request.setCode("LAB-" + UUID.randomUUID());
        request.setLocation("A-102");
        request.setArea(60.0);
        request.setCapacity(40);
        request.setType("普通实验室");
        request.setStatus(1);
        request.setManagerId(null);
        request.setDescription("用于测试的实验室2");

        mockMvc.perform(post("/api/v1/laboratory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").isNumber());
    }

    @Test
    @DisplayName("PUT /api/v1/laboratory/{id} - 更新实验室")
    @WithMockUser(roles = "admin")
    void updateLaboratory() throws Exception {
        assertThat(createdLabId).isNotNull();

        UpdateLaboratoryRequest request = new UpdateLaboratoryRequest();
        request.setName("更新后的实验室名称");

        mockMvc.perform(put("/api/v1/laboratory/{id}", createdLabId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("更新后的实验室名称"));
    }

    @Test
    @DisplayName("PUT /api/v1/laboratory/{id}/status - 更新实验室状态")
    @WithMockUser(roles = "admin")
    void updateLaboratoryStatus() throws Exception {
        assertThat(createdLabId).isNotNull();

        UpdateStatusRequest request = new UpdateStatusRequest();
        request.setStatus(2);

        mockMvc.perform(put("/api/v1/laboratory/{id}/status", createdLabId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value(2));
    }

    @Test
    @DisplayName("GET /api/v1/laboratory/{id}/equipment - 获取实验室设备列表")
    @WithMockUser(roles = {"admin", "teacher"})
    void getLaboratoryEquipment() throws Exception {
        assertThat(createdLabId).isNotNull();

        mockMvc.perform(get("/api/v1/laboratory/{id}/equipment", createdLabId)
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("DELETE /api/v1/laboratory/{id} - 删除实验室")
    @WithMockUser(roles = "admin")
    void deleteLaboratory() throws Exception {
        // 先新建一个实验室再删除，避免影响其它用例
        CreateLaboratoryRequest request = new CreateLaboratoryRequest();
        request.setName("待删除实验室");
        request.setCode("LAB-DEL-" + UUID.randomUUID());
        request.setLocation("B-201");
        request.setArea(30.0);
        request.setCapacity(20);
        request.setType("普通实验室");
        request.setStatus(1);
        request.setManagerId(null);
        request.setDescription("删除用实验室");

        MvcResult result = mockMvc.perform(post("/api/v1/laboratory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        mockMvc.perform(delete("/api/v1/laboratory/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}


