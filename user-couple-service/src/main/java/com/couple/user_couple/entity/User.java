package com.couple.user_couple.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "couple_id", nullable = true)
    private UUID coupleId;

    @Column(name = "score", nullable = true)
    @Builder.Default
    private Long score = 500L;

    @Column(name = "gender", nullable = false, length = 1)
    private String gender;

    @Column(name = "send_time", nullable = false)
    private String sendTime;

    @Column(name = "fcm_code", nullable = false, length = 50)
    private String fcmCode;

    @Column(name = "birth", nullable = false)
    private String birth;

    @Column(name = "year")
    private Integer year;

    @Column(name = "month")
    private Integer month;

    @Column(name = "date")
    private Integer date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "couple_id", insertable = false, updatable = false)
    private Couple couple;

}