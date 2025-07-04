package com.couple.question_answer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "question_tags")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionTag {

    @EmbeddedId
    private QuestionTagId id;

    @Column(name = "value", nullable = false)
    private Float value;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("questionId")
    @JoinColumn(name = "question_id")
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tagId")
    @JoinColumn(name = "tag_id")
    private Tag tag;
}