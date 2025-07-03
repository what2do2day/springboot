package com.couple.schedule_meeting.entity;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "meeting_keywords")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetingKeyword {
    @EmbeddedId
    private MeetingKeywordId id;

    @Column(name = "sequence")
    private Integer sequence;

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MeetingKeywordId implements Serializable {
        @Column(name = "meeting_id")
        private UUID meetingId;

        @Column(name = "keyword_id")
        private UUID keywordId;
    }
} 