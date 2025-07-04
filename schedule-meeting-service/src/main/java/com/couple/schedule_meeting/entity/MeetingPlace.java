package com.couple.schedule_meeting.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "meeting_places")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetingPlace {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "meeting_id", nullable = false)
    private UUID meetingId;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "address", length = 200)
    private String address;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "latitude", precision = 15, scale = 13)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 15, scale = 13)
    private BigDecimal longitude;

    @Column(name = "sequence")
    private Integer sequence;
} 