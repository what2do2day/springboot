package com.couple.schedule_meeting.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "meeting_places")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingPlace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @Column(nullable = false)
    private String name;

    @Column(name = "place_id")
    private String placeId;

    @Column(name = "address")
    private String address;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "visit_order")
    private Integer visitOrder;

    @Column(name = "estimated_time")
    private Integer estimatedTime; // 분 단위

    @Column(name = "category")
    private String category;
}