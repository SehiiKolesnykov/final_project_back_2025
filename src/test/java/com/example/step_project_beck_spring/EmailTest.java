package com.example.step_project_beck_spring;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
@SpringBootTest(properties = {
        /* 1. ЗАГЛУШКА ДЛЯ CLOUDINARY (Щоб не падало) */
        "cloudinary.cloud-name=test-cloud",
        "cloudinary.api-key=12345",
        "cloudinary.api-secret=secret",

        /* 2. НАЛАШТУВАННЯ ПОШТИ (Щоб створився JavaMailSender) */
        "spring.mail.host=smtp.gmail.com",
        "spring.mail.port=587",
        "spring.mail.username=galaykseniia@gmail.com",
        "spring.mail.password=slmnnamangrccolj",
        "spring.mail.properties.mail.smtp.auth=true",
        "spring.mail.properties.mail.smtp.starttls.enable=true"
})
public class EmailTest {

    @Autowired
    private JavaMailSender mailSender;

    @Test
    void sendTestEmail() {
        SimpleMailMessage message = new SimpleMailMessage();

        /* От кого */
        message.setFrom("galaykseniia@gmail.com");

        /* Кому */
        message.setTo("step-project@proton.me");

        message.setSubject("Test Spring Boot");
        message.setText("Вітаю! Це твій код: SMTH");

        mailSender.send(message);
        System.out.println("Letter has been send successfully!");
    }
}