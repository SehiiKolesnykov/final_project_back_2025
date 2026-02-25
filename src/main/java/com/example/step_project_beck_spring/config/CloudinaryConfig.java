package com.example.step_project_beck_spring.config;

import com.cloudinary.Cloudinary;
import lombok.Getter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * Конфігурація Cloudinary.
 * api_secret НІКОЛИ не витікає на фронтенд!
 */
@Configuration
@Getter
public class CloudinaryConfig {

    private final String cloudName;
    private final String apiKey;
    private final String apiSecret;

    public CloudinaryConfig(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret) {
        this.cloudName = cloudName;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(Map.of(
                "cloud_name", cloudName,     // назва хмарного акаунту
                "api_key", apiKey,              // публічний ключ API
                "api_secret", apiSecret         // секретний ключ
        ));
    }
}
