package com.couple.schedule_meeting.exception;

import java.util.UUID;

public class ScheduleAccessDeniedException extends RuntimeException {
    public ScheduleAccessDeniedException(String message) {
        super(message);
    }
    
    public ScheduleAccessDeniedException(UUID scheduleId, UUID coupleId) {
        super("Access denied to schedule with id: " + scheduleId + " for couple: " + coupleId);
    }
} 