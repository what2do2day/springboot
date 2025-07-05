package com.couple.schedule_meeting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.couple.schedule_meeting.repository")
public class ScheduleMeetingApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScheduleMeetingApplication.class, args);
    }
}