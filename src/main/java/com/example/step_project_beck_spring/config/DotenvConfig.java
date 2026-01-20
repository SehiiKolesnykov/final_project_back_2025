//package com.example.step_project_beck_spring.config;
//
//import io.github.cdimascio.dotenv.Dotenv;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
///**
// * Конфігурація для завантаження змінних оточення з файлу .env
// * Використовується лише під час розробки (локально), бо в продакшені будемо
// * додавати через змінні оточення сервера, а не через файл!
// */
//@Configuration
//public class DotenvConfig {
//
//    /**
//     * Створює та повертає єдиний екземпляр Dotenv як Spring-бін.
//     *
//     * Після цього в будь-якому іншому @Component / @Service / @Configuration тощо
//     * можна просто інжектити:
//     *     private final Dotenv dotenv;
//     *
//     * Або використовувати разом з @Value, наприклад:
//     * @Value("${DATABASE_URL}") String dbUrl; — значення підтягнеться з .env через Dotenv
//     *
//     * Dotenv.load() шукає файл .env у корені проєкту і завантажує всі змінні з нього.
//     * Якщо файл відсутній — просто нічого не завантажить, помилки не буде.
//     */
//    @Bean
//    public Dotenv dotenv() {
//        return Dotenv.load();
//    }
//}