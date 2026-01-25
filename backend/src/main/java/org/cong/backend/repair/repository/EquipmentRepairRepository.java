package org.cong.backend.repair.repository;

import org.cong.backend.repair.entity.EquipmentRepair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EquipmentRepairRepository extends JpaRepository<EquipmentRepair, Long>, JpaSpecificationExecutor<EquipmentRepair> {
}

