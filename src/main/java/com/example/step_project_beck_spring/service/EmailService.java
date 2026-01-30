package com.example.step_project_beck_spring.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;  // ← це правильний клас!
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    @Value("${resend.api-key}")
    private String apiKey;

    @Value("${resend.from-email}")
    private String fromEmail;

    /**
     * Відправляє email з кодом підтвердження через Resend API.
     */
    public void sendVerificationEmail(String toEmail, String code) {
        try {
            Resend resend = new Resend(apiKey);

            // Той самий красивий HTML-шаблон
            String htmlContent = """
                <div style="font-family: Arial, sans-serif; padding: 20px; border: 1px solid #ddd; border-radius: 10px;">
                    <h2 style="color: #2c3e50;">Вітаємо у Step Project!</h2>
                    <p>Дякуємо за реєстрацію. Будь ласка, введіть цей код для підтвердження вашого email:</p>
                    <h1 style="color: #27ae60; letter-spacing: 5px;">%s</h1>
                    <p>Якщо ви не реєструвалися, просто проігноруйте цей лист.</p>
                </div>
                """.formatted(code);

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(fromEmail)  // Рекомендую формат: "Step Project <noreply@yourdomain.com>"
                    .to(toEmail)
                    .subject("Підтвердження реєстрації")
                    .html(htmlContent)
                    .build();

            // Відправка та отримання відповіді
            CreateEmailResponse response = resend.emails().send(params);

            // response.getId() — це string, наприклад "26abdd24-..."
            if (response.getId() != null && !response.getId().isEmpty()) {
                log.info("Verification email sent to: {} via Resend. Email ID: {}", toEmail, response.getId());
            } else {
                log.warn("Resend returned empty ID for email to {}", toEmail);
                // все одно вважаємо успішним, бо помилки кидаються винятком
            }

        } catch (ResendException e) {
            // ResendException містить код помилки та повідомлення від API
            log.error("Resend API error sending to {}: {} (code: {})",
                    toEmail, e.getMessage(), e.getStatusCode(), e);
            throw new RuntimeException("Не вдалося відправити email через Resend: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error sending email to {}", toEmail, e);
            throw new RuntimeException("Не вдалося відправити email", e);
        }
    }
}