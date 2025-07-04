package com.couple.user_couple.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "couples")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Couple {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "character_id", length = 50, columnDefinition = "varchar(50) default 'C000'")
    @Builder.Default
    private String characterId = "C000";

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "custom_date")
    private LocalDate customDate;

    @Column(name = "year")
    private Integer year;

    @Column(name = "month")
    private Integer month;

    @Column(name = "date")
    private Integer date;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    @Column(name = "expired", length = 1)
    @Builder.Default
    private String expired = "N";

    @Column(name = "total_scores", nullable = false)
    @Builder.Default
    private Long totalScores = 0L;

    @OneToMany(mappedBy = "couple", fetch = FetchType.LAZY)
    private List<User> users;

}