package com.couple.schedule_meeting.repository;

import com.couple.schedule_meeting.entity.Route;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RouteRepository extends MongoRepository<Route, String> {
    // 기본 CRUD 메서드들은 MongoRepository에서 제공됨
    // 필요에 따라 커스텀 쿼리 메서드 추가 가능
} 