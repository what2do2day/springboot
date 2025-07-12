package com.couple.couple_chat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.couple.couple_chat")
public class CoupleChatApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoupleChatApplication.class, args);
    }
} 