package com.couple.question_answer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionResponse {

    private UUID id;
    private String question;
    private String option1;
    private String option2;
    private String sentYn;
    private LocalDateTime sentTime;
    private LocalDate date;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}