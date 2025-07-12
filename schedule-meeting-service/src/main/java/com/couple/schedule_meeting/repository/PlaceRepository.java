package com.couple.schedule_meeting.repository;

import com.couple.schedule_meeting.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaceRepository extends JpaRepository<Place, String> {
    Optional<Place> findByName(String name);
    
    @Query("SELECT p FROM Place p WHERE p.code = :code AND p.address LIKE %:region% ORDER BY p.rating DESC")
    List<Place> findByCodeAndAddressContainingOrderByRatingDesc(@Param("code") String code, @Param("region") String region);
    
    // ID 순서로 정렬 (저장 순서와 유사)
    @Query("SELECT p FROM Place p WHERE p.code = :code AND p.address LIKE %:region% ORDER BY p.id ASC")
    List<Place> findByCodeAndAddressContainingOrderByIdAsc(@Param("code") String code, @Param("region") String region);
} 