package com.couple.question_answer.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions.MongoConverterConfigurationAdapter;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.util.UUID;
import java.util.List;

@Configuration
@EnableMongoRepositories(basePackages = "com.couple.question_answer")
public class MongoConfig {

    @Value("${spring.data.mongodb.uri:}")
    private String mongoUri;

    @Bean
    public MongoMappingContext mongoMappingContext() {
        return new MongoMappingContext();
    }

    @Bean
    public MongoCustomConversions customConversions() {
        return new MongoCustomConversions(List.of(
            // UUID를 문자열로 변환
            new org.springframework.core.convert.converter.Converter<UUID, String>() {
                @Override
                public String convert(UUID source) {
                    return source != null ? source.toString() : null;
                }
            },
            // 문자열을 UUID로 변환
            new org.springframework.core.convert.converter.Converter<String, UUID>() {
                @Override
                public UUID convert(String source) {
                    return source != null ? UUID.fromString(source) : null;
                }
            }
        ));
    }

    @Bean
    public MongoClient mongoClient() {
        if (mongoUri.isEmpty()) {
            // For local development, use default configuration
            return MongoClients.create();
        }

        // For production with Amazon DocumentDB, disable SSL certificate validation
        try {
            // Create a trust manager that trusts all certificates
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
            };

            // Create SSL context with the trust manager
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            // Configure MongoClientSettings
            MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(mongoUri))
                .applyToSslSettings(builder -> 
                    builder.enabled(true)
                          .context(sslContext)
                          .invalidHostNameAllowed(true))
                .build();

            return MongoClients.create(settings);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create MongoClient", e);
        }
    }
} 