package com.couple.schedule_meeting.exception;

import java.util.UUID;

public class MeetingAccessDeniedException extends RuntimeException {
    public MeetingAccessDeniedException(String message) {
        super(message);
    }
    
    public MeetingAccessDeniedException(UUID meetingId, UUID coupleId) {
        super("Access denied to meeting with id: " + meetingId + " for couple: " + coupleId);
    }
} 