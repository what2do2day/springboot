package com.couple.schedule_meeting.dto;

import java.util.List;

public class ScheduleCalendarResponse {
    private int year;
    private int month;
    private List<DateInfo> dates;

    public ScheduleCalendarResponse(int year, int month, List<DateInfo> dates) {
        this.year = year;
        this.month = month;
        this.dates = dates;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public List<DateInfo> getDates() {
        return dates;
    }

    public static class DateInfo {
        private int date;
        private String type;
        private String title; // meeting name, birthday name, anniversary text
        private String detail; // meetingId, userId, null for anniversary

        public DateInfo(int date, String type, String title, String detail) {
            this.date = date;
            this.type = type;
            this.title = title;
            this.detail = detail;
        }

        public int getDate() {
            return date;
        }

        public String getType() {
            return type;
        }

        public String getTitle() {
            return title;
        }

        public String getDetail() {
            return detail;
        }
    }
} 