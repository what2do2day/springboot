package com.couple.schedule_meeting.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "places")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Place {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Column(name = "address", length = 100, nullable = false)
    private String address;

    @Column(name = "rating", precision = 2, scale = 1)
    private BigDecimal rating;

    @Column(name = "category", length = 100, nullable = false)
    private String category;

    @Column(name = "latitude", precision = 15, scale = 13)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 15, scale = 13)
    private BigDecimal longitude;
} 