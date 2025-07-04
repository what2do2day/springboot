package com.couple.question_answer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "questions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "question", nullable = false, length = 100)
    private String question;

    @Column(name = "option1", nullable = false, length = 100)
    private String option1;

    @Column(name = "option2", nullable = false, length = 100)
    private String option2;

    @Column(name = "sent_yn", nullable = false, length = 1)
    @Builder.Default
    private String sentYn = "N";

    @Column(name = "sent_time")
    private LocalDateTime sentTime;

    @Column(name = "date")
    private LocalDate date;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}