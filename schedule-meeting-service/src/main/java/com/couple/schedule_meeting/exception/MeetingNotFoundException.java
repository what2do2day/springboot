package com.couple.schedule_meeting.exception;

import java.util.UUID;

public class MeetingNotFoundException extends RuntimeException {
    public MeetingNotFoundException(String message) {
        super(message);
    }
    
    public MeetingNotFoundException(UUID meetingId) {
        super("Meeting not found with id: " + meetingId);
    }
} 