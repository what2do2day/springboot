package com.couple.schedule_meeting.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.util.UUID;

@Entity
@Table(name = "meeting_keywords")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetingKeyword {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "meeting_id", nullable = false)
    private UUID meetingId;

    @Column(name = "keyword", length = 100, nullable = false)
    private String keyword;

    @Column(name = "sequence")
    private Integer sequence;
} 