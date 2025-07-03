package com.couple.schedule_meeting.entity;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "meeting_places")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetingPlace {
    @EmbeddedId
    private MeetingPlaceId id;

    @Column(name = "sequence")
    private Integer sequence;

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MeetingPlaceId implements Serializable {
        @Column(name = "meeting_id")
        private UUID meetingId;

        @Column(name = "place_id")
        private UUID placeId;
    }
} 