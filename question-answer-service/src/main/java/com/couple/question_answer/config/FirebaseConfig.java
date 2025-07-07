package com.couple.question_answer.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
@Slf4j
public class FirebaseConfig {
    
    @Bean
    public FirebaseApp firebaseApp() {
        // 이미 초기화된 FirebaseApp이 있는지 확인
        if (FirebaseApp.getApps().isEmpty()) {
            try {
                // Firebase Admin SDK JSON 파일 로드
                InputStream serviceAccount = new FileInputStream("/app/firebase-adminsdk.json");
                
                GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
                
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(credentials)
                        .build();
                
                FirebaseApp app = FirebaseApp.initializeApp(options);
                log.info("Firebase App initialized successfully");
                return app;
                
            } catch (IOException e) {
                log.error("Failed to initialize Firebase App", e);
                throw new RuntimeException("Firebase initialization failed", e);
            }
        } else {
            log.info("Firebase App already initialized, returning existing instance");
            return FirebaseApp.getInstance();
        }
    }
    
    @Bean
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }
} 