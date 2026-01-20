package org.cong.backend.equipment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cong.backend.equipment.entity.EquipmentStatus;
import org.cong.backend.equipment.dto.CreateEquipmentRequest;
import org.cong.backend.equipment.dto.UpdateEquipmentRequest;
import org.cong.backend.equipment.dto.UpdateEquipmentStatusRequest;
import org.cong.backend.equipment.repository.EquipmentStatusRepository;
import org.cong.backend.laboratory.entity.Laboratory;
import org.cong.backend.laboratory.repository.LaboratoryRepository;
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
class EquipmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LaboratoryRepository laboratoryRepository;

    @Autowired
    private EquipmentStatusRepository equipmentStatusRepository;

    private Laboratory pickAvailableLaboratory() {
        return laboratoryRepository.findAll().stream()
                .filter(lab -> lab.getStatus() != null && lab.getStatus() == 1)
                .findFirst()
                .orElseGet(() -> {
                    // 若真实库没有可用实验室，则在测试中补一条（避免受外部数据影响）
                    Laboratory lab = new Laboratory();
                    lab.setName("测试实验室");
                    lab.setCode("LAB-TEST-" + UUID.randomUUID().toString().substring(0, 8));
                    lab.setLocation("测试楼-测试房间");
                    lab.setType("测试类型");
                    lab.setStatus(1);
                    lab.setDescription("用于自动化测试创建的实验室");
                    lab.setCreateTime(LocalDateTime.now());
                    lab.setUpdateTime(LocalDateTime.now());
                    return laboratoryRepository.save(lab);
                });
    }

    private EquipmentStatus pickDefaultStatus() {
        // 优先选择 instored（与前端默认一致）；没有则退回 pending
        return equipmentStatusRepository.findByCode("instored")
                .or(() -> equipmentStatusRepository.findByCode("pending"))
                .orElseGet(() -> {
                    EquipmentStatus status = new EquipmentStatus();
                    status.setName("已入库");
                    status.setCode("instored");
                    status.setDescription("用于自动化测试创建的设备状态");
                    return equipmentStatusRepository.save(status);
                });
    }

    private Long createEquipmentForTest() throws Exception {
        CreateEquipmentRequest request = new CreateEquipmentRequest();
        request.setName("测试设备");
        request.setModel("MODEL-001");
        request.setSpecification("SPEC-001");
        // 使用随机资产编号，避免与数据库中已有数据冲突
        request.setAssetCode("ASSET-" + UUID.randomUUID());
        request.setUnitPrice(1000.0);
        request.setQuantity(1);
        request.setSupplier("供应商A");
        request.setPurchaseDate(LocalDate.now());
        request.setWarrantyPeriod(12);
        EquipmentStatus status = pickDefaultStatus();
        Laboratory lab = pickAvailableLaboratory();
        request.setStatusId(status.getId());
        request.setLaboratoryId(lab.getId());
        request.setDescription("用于测试的设备");

        MvcResult result = mockMvc.perform(post("/api/v1/equipment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("id").asLong();
    }

    @Test
    @DisplayName("GET /api/v1/equipment - 设备列表查询")
    @WithMockUser(roles = {"admin", "teacher", "student"})
    void getEquipmentList() throws Exception {
        mockMvc.perform(get("/api/v1/equipment")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list").isArray());
    }

    @Test
    @DisplayName("POST /api/v1/equipment - 创建设备")
    @WithMockUser(roles = "admin")
    void createEquipment() throws Exception {
        Long id = createEquipmentForTest();

        mockMvc.perform(get("/api/v1/equipment/{id}", id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(id));
    }

    @Test
    @DisplayName("GET /api/v1/equipment/{id} - 设备详情查询")
    @WithMockUser(roles = {"admin", "teacher", "student"})
    void getEquipmentById() throws Exception {
        Long id = createEquipmentForTest();

        mockMvc.perform(get("/api/v1/equipment/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(id));
    }

    @Test
    @DisplayName("PUT /api/v1/equipment/{id} - 更新设备")
    @WithMockUser(roles = "admin")
    void updateEquipment() throws Exception {
        Long id = createEquipmentForTest();

        UpdateEquipmentRequest request = new UpdateEquipmentRequest();
        request.setName("更新后的设备名称");

        mockMvc.perform(put("/api/v1/equipment/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("更新后的设备名称"));
    }

    @Test
    @DisplayName("PUT /api/v1/equipment/{id}/status - 更新设备状态")
    @WithMockUser(roles = {"admin", "teacher"})
    void updateEquipmentStatus() throws Exception {
        Long id = createEquipmentForTest();

        UpdateEquipmentStatusRequest request = new UpdateEquipmentStatusRequest();
        // 设置为当前状态（等价更新），应允许
        request.setStatusId(pickDefaultStatus().getId());

        mockMvc.perform(put("/api/v1/equipment/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("DELETE /api/v1/equipment/{id} - 删除设备")
    @WithMockUser(roles = "admin")
    void deleteEquipment() throws Exception {
        Long id = createEquipmentForTest();

        mockMvc.perform(delete("/api/v1/equipment/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("DELETE /api/v1/equipment/batch - 批量删除设备")
    @WithMockUser(roles = "admin")
    void batchDeleteEquipment() throws Exception {
        Long id1 = createEquipmentForTest();
        Long id2 = createEquipmentForTest();

        String body = """
                {"ids":[%d,%d]}
                """.formatted(id1, id2);

        mockMvc.perform(delete("/api/v1/equipment/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("GET /api/v1/equipment/status - 获取设备状态列表")
    @WithMockUser(roles = {"admin", "teacher", "student"})
    void getEquipmentStatusList() throws Exception {
        mockMvc.perform(get("/api/v1/equipment/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }
}


