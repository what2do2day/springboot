package com.couple.schedule_meeting.exception;

import java.util.UUID;

public class ScheduleNotFoundException extends RuntimeException {
    public ScheduleNotFoundException(String message) {
        super(message);
    }
    
    public ScheduleNotFoundException(UUID scheduleId) {
        super("Schedule not found with id: " + scheduleId);
    }
} 