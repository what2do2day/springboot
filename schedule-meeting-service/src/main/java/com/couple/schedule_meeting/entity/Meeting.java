package com.couple.schedule_meeting.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "meetings")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Meeting {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "couple_id", nullable = false)
    private UUID coupleId;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "startTime", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "endTime", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "date", length = 50, nullable = false)
    private String date;

    @Column(name = "year", insertable = false, updatable = false)
    private Integer year;

    @Column(name = "month", insertable = false, updatable = false)
    private Integer month;

    @Column(name = "date", insertable = false, updatable = false)
    private Integer day;
} 