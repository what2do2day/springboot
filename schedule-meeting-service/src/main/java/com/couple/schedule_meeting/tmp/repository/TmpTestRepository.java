package com.couple.schedule_meeting.tmp.repository;

import com.couple.schedule_meeting.tmp.entity.TmpTestEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TmpTestRepository extends MongoRepository<TmpTestEntity, String> {
} 