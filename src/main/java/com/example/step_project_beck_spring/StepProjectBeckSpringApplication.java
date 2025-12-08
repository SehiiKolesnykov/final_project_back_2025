package com.example.step_project_beck_spring;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

/**
 * @EntityScan — явно вказує Spring Boot, де шукати JPA-ентіті (потрібно, бо пакет entities
 *              лежить глибше, ніж основний пакет додатку)
 */
@SpringBootApplication
@EntityScan("com.example.step_project_beck_spring.entities") // без цього Spring чомусь перестав бачити @Entity-класи
public class StepProjectBeckSpringApplication {

    /**
     * Чому це в main():
     * - Spring ще не стартував → @Bean з DotenvConfig ще не створено
     * - Тому вручну завантажуємо .env і передаємо всі змінні в System.properties
     * - Spring автоматично бере змінні з System.properties, якщо вони є
     */
    public static void main(String[] args) {

        // Налаштування та завантаження .env-файлу
        Dotenv dotenv = Dotenv.configure()
                .directory("./")           // шукати .env у корені проєкту (поруч з pom.xml/gradle)
                .ignoreIfMalformed()       // не падати, якщо в .env є некоректні рядки
                .ignoreIfMissing()         // не падати, якщо файлу .env взагалі немає (наприклад у проді)
                .load();                   // саме завантаження

        // Копіюємо всі змінні з .env у System.properties — тепер Spring їх побачить через @Value("${...}")
        dotenv.entries().forEach(entry ->
                System.setProperty(entry.getKey(), entry.getValue())
        );

        // Запускаємо Spring Boot додаток
        SpringApplication.run(StepProjectBeckSpringApplication.class, args);
    }
}