package com.couple.mission_store.repository;

import com.couple.mission_store.entity.CoupleMission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CoupleMissionRepository extends JpaRepository<CoupleMission, UUID> {

    List<CoupleMission> findByCoupleIdOrderByAssignedDateDesc(UUID coupleId);

    Optional<CoupleMission> findByCoupleIdAndAssignedDate(UUID coupleId, LocalDate assignedDate);

    @Query("SELECT cm FROM CoupleMission cm WHERE cm.coupleId = :coupleId AND cm.assignedDate = :assignedDate AND cm.completed = 'N'")
    Optional<CoupleMission> findIncompleteMissionByCoupleIdAndDate(@Param("coupleId") UUID coupleId,
            @Param("assignedDate") LocalDate assignedDate);

    List<CoupleMission> findByCoupleIdAndCompletedOrderByAssignedDateDesc(UUID coupleId, String completed);
}