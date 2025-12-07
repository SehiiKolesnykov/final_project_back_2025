package com.example.step_project_beck_spring.config;

import com.example.step_project_beck_spring.entities.User;
import com.example.step_project_beck_spring.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.time.LocalDate;
import java.util.UUID;

@Configuration
@EnableWebSecurity

public class SecurityConfig {

    /**
     * BCrypt — алгоритм для паролів.
     * 10–12 — я вважаю що цього буде достатньо.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Bean, який встановлює правила доступу (Security Filter Chain) та дозволяє публічний доступ до всіх GET-запитів на /api/user/
     ***/
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        /** Дозволяємо публічний доступ до User */
                        .requestMatchers("/api/user/**").permitAll()
                        .anyRequest().authenticated()
                )
                /** Вимкнення захисту CSRF (потрібно для коректної роботи REST API) */
                .csrf(csrf -> csrf.disable())
                /** Відключення стандартних форм аутентифікації */
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        return http.build();
    }
    /** Додаю тестового користувача до бази даних при старті програми.
     * @param userRepository Репозиторій для збереження даних
     * @param passwordEncoder Кодувальник паролів
     */
    /**
     * @Bean public CommandLineRunner initData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
     * // return args -> {
     * UUID testUuid = UUID.fromString("f2b05697-b892-4f33-8a9d-598d9c2a6f11");
     * <p>
     * if (userRepository.findById(testUuid).isEmpty()) {
     * <p>
     * User testUser = new User();
     * testUser.setId(testUuid);
     * testUser.setFirstName("Oleksii");
     * testUser.setLastName("Zharkov");
     * testUser.setEmail("test@example.com");
     * <p>
     * testUser.setPassword(passwordEncoder.encode("securepassword"));
     * testUser.setBirthDate(LocalDate.of(2001, 1, 1));
     * testUser.setEmailVerified(true);
     * <p>
     * userRepository.save(testUser);
     * System.out.println(" TEST: Oleksii Zharkov.");
     * }
     * };
     * }
      Це можете перевірити та видалити бо то для тесту робив*/
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        String hash = encoder.encode("securepassword");
        System.out.println("ХЕШ ДЛЯ ВСТАВКИ: " + hash);
    }
}