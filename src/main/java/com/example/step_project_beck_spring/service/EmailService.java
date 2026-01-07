package com.example.step_project_beck_spring.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j // Log (щоб бачити в консолі, що лист пішов)
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /** Відправляє код підтвердження, зробив HTML бо гарніше виглядає */
    public void sendVerificationEmail(String toEmail, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Підтвердження реєстрації");

            // HTML шаблон листа
            String htmlContent = """
                <div style="font-family: Arial, sans-serif; padding: 20px; border: 1px solid #ddd; border-radius: 10px;">
                    <h2 style="color: #2c3e50;">Вітаємо у Step Project!</h2>
                    <p>Дякуємо за реєстрацію. Будь ласка, введіть цей код для підтвердження вашого email:</p>
                    <h1 style="color: #27ae60; letter-spacing: 5px;">%s</h1>
                    <p>Якщо ви не реєструвалися, просто проігноруйте цей лист.</p>
                </div>
                """.formatted(code);

            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Verification email sent to: {}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send email", e);
            throw new RuntimeException("Не вдалося відправити email", e);
        }
    }
}