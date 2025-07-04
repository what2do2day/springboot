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
        private String title; // optional, for meeting/schedule
        private String name;  // optional, for birthday

        public DateInfo(int date, String type, String title, String name) {
            this.date = date;
            this.type = type;
            this.title = title;
            this.name = name;
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

        public String getName() {
            return name;
        }
    }
} 