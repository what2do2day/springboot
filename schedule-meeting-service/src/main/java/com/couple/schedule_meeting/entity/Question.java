package com.couple.schedule_meeting.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "questions")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "question", length = 100, nullable = false)
    private String question;

    @Column(name = "option1", length = 100, nullable = false)
    private String option1;

    @Column(name = "option2", length = 100, nullable = false)
    private String option2;

    @Column(name = "sentYn", length = 1)
    private String sentYn;

    @Column(name = "sentTime")
    private LocalDateTime sentTime;

    @Column(name = "date")
    private String date;
}