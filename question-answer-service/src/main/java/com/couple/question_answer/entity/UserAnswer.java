package com.couple.question_answer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_answers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "question_id", nullable = false)
    private String questionId; // MongoDB ObjectId를 문자열로 저장

    @Column(name = "couple_id", nullable = true)
    private UUID coupleId;

    @Column(name = "selected_choice", nullable = false, length = 1)
    private String selectedChoice; // 'A' 또는 'B'

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
}