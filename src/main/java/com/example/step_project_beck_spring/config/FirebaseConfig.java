package com.example.step_project_beck_spring.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        String jsonCredentials = System.getenv("FIREBASE_SERVICE_ACCOUNT_JSON");

        if (jsonCredentials == null || jsonCredentials.trim().isEmpty()) {
            throw new IllegalStateException(
                    "FIREBASE_SERVICE_ACCOUNT_JSON environment variable is not set or empty!"
            );
        }

        // Перетворюємо рядок у InputStream
        ByteArrayInputStream serviceAccountStream = new ByteArrayInputStream(
                jsonCredentials.getBytes(StandardCharsets.UTF_8)
        );

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
                .build();

        // Якщо хочеш вказати явно ім'я апки (опціонально)
        return FirebaseApp.initializeApp(options, "my-firebase-app");
        // або просто FirebaseApp.initializeApp(options);
    }
}