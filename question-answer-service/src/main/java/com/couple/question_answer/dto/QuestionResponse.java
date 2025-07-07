package com.couple.question_answer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionResponse {

    private String id;
    private String question;
    private LocalDate date;
    private String choiceA;
    private String choiceB;
    private List<String> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}