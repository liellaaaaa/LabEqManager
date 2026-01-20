package org.cong.backend.equipment.repository;

import org.cong.backend.equipment.entity.EquipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EquipmentStatusRepository extends JpaRepository<EquipmentStatus, Long> {

    Optional<EquipmentStatus> findByCode(String code);

}

