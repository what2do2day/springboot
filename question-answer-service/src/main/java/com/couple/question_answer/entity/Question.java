package com.couple.question_answer.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "questions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Question {

    @Id
    private String id;

    @Field("question")
    private String question;

    @Field("date")
    private LocalDate date;

    @Field("choice_a")
    private String choice_a;

    @Field("vectors_a")
    private List<VectorChange> vectors_a;

    @Field("choice_b")
    private String choice_b;

    @Field("vectors_b")
    private List<VectorChange> vectors_b;

    @Field("tags")
    private List<String> tags;

    @Field("createdAt")
    private LocalDateTime createdAt;

    @Field("updatedAt")
    private LocalDateTime updatedAt;
}