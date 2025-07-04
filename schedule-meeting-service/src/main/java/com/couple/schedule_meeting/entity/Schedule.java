package com.couple.schedule_meeting.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "schedules")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Schedule {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "couple_id", nullable = false)
    private UUID coupleId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "message")
    private String message;

    @Column(name = "dateTime", nullable = false)
    private LocalDateTime dateTime;

    @Column(name = "year", insertable = false, updatable = false,
            columnDefinition = "INTEGER GENERATED ALWAYS AS (EXTRACT(YEAR FROM date_time)) STORED")
    private Integer year;

    @Column(name = "month", insertable = false, updatable = false,
            columnDefinition = "INTEGER GENERATED ALWAYS AS (EXTRACT(MONTH FROM date_time)) STORED")
    private Integer month;

    @Column(name = "day", insertable = false, updatable = false,
            columnDefinition = "INTEGER GENERATED ALWAYS AS (EXTRACT(DAY FROM date_time)) STORED")
    private Integer day;

} 