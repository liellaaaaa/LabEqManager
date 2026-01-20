package org.cong.backend.equipment.service;

import org.cong.backend.equipment.dto.EquipmentStatusResponse;
import org.cong.backend.equipment.entity.EquipmentStatus;
import org.cong.backend.equipment.repository.EquipmentStatusRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EquipmentStatusService {

    private final EquipmentStatusRepository equipmentStatusRepository;

    public EquipmentStatusService(EquipmentStatusRepository equipmentStatusRepository) {
        this.equipmentStatusRepository = equipmentStatusRepository;
    }

    public List<EquipmentStatusResponse> getAllStatuses() {
        List<EquipmentStatus> statuses = equipmentStatusRepository.findAll();
        return statuses.stream()
                .map(this::toEquipmentStatusResponse)
                .collect(Collectors.toList());
    }

    private EquipmentStatusResponse toEquipmentStatusResponse(EquipmentStatus status) {
        EquipmentStatusResponse response = new EquipmentStatusResponse();
        response.setId(status.getId());
        response.setName(status.getName());
        response.setCode(status.getCode());
        response.setDescription(status.getDescription());
        return response;
    }
}

