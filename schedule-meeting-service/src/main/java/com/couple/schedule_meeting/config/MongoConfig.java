package com.couple.schedule_meeting.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "com.couple.schedule_meeting.repository")
public class MongoConfig {

    @Bean
    public MongoMappingContext mongoMappingContext() {
        return new MongoMappingContext();
    }
} 