package org.cong.backend.scrap.repository;

import org.cong.backend.scrap.entity.EquipmentScrap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EquipmentScrapRepository extends JpaRepository<EquipmentScrap, Long>, JpaSpecificationExecutor<EquipmentScrap> {
}

