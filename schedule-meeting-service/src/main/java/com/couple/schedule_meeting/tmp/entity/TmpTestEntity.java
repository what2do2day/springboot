package com.couple.schedule_meeting.tmp.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "tmp_test")
public class TmpTestEntity {
    
    @Id
    private String id;
    
    private String text;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 