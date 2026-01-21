package org.cong.backend.borrow.repository;

import org.cong.backend.borrow.entity.EquipmentBorrow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EquipmentBorrowRepository extends JpaRepository<EquipmentBorrow, Long>, JpaSpecificationExecutor<EquipmentBorrow> {

    @Query("select coalesce(sum(b.quantity), 0) from EquipmentBorrow b where b.equipmentId = :equipmentId and b.status in (3,5)")
    int sumBorrowedQuantity(@Param("equipmentId") Long equipmentId);

    List<EquipmentBorrow> findByStatusAndPlanReturnDateBefore(Integer status, LocalDateTime time);
}


