package com.example.step_project_beck_spring.service;

import brevo.ApiClient;
import brevo.ApiException;
import brevoApi.TransactionalEmailsApi;
import brevoModel.SendSmtpEmail;
import brevoModel.SendSmtpEmailSender;
import brevoModel.SendSmtpEmailTo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Сервіс для відправки email через Brevo (ex-Sendinblue) API.
 * Використовується для надсилання коду верифікації при реєстрації.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    @Value("${brevo.api-key}")
    private String apiKey;

    @Value("${brevo.from-email}")
    private String fromEmail;

    /**
     * Відправляє email з кодом підтвердження.
     *
     * @param toEmail адреса отримувача
     * @param code    6-значний код верифікації
     * @throws RuntimeException якщо відправка не вдалася
     */
    public void sendVerificationEmail(String toEmail, String code) {
        // Налаштування клієнта Brevo
        ApiClient defaultClient = new ApiClient();
        defaultClient.setApiKeyPrefix("api-key");
        defaultClient.setApiKey(apiKey);

        brevoApi.TransactionalEmailsApi apiInstance = new TransactionalEmailsApi(defaultClient);

        // Відправник (має бути верифікований у Brevo)
        SendSmtpEmailSender sender = new SendSmtpEmailSender()
                .email(fromEmail)
                .name("Step Project");  // ім'я, яке бачить отримувач

        // Отримувач
        SendSmtpEmailTo to = new SendSmtpEmailTo().email(toEmail);

        // HTML-вміст листа (той самий, що був раніше)
        String htmlContent = """
            <div style="font-family: Arial, sans-serif; padding: 20px; border: 1px solid #ddd; border-radius: 10px; max-width: 600px; margin: 0 auto;">
                <h2 style="color: #2c3e50; text-align: center;">Вітаємо у Step Project!</h2>
                <p style="font-size: 16px;">Дякуємо за реєстрацію.</p>
                <p style="font-size: 16px;">Будь ласка, введіть цей код для підтвердження вашого email:</p>
                <h1 style="color: #27ae60; letter-spacing: 8px; text-align: center; font-size: 40px; margin: 30px 0;">%s</h1>
                <p style="font-size: 14px; color: #555; text-align: center;">
                    Код дійсний 10 хвилин. Якщо ви не реєструвалися — просто проігноруйте цей лист.
                </p>
            </div>
            """.formatted(code);

        // Повний об'єкт для відправки
        SendSmtpEmail sendSmtpEmail = new SendSmtpEmail()
                .sender(sender)
                .to(Collections.singletonList(to))
                .subject("Підтвердження реєстрації в Step Project")
                .htmlContent(htmlContent);

        try {
            apiInstance.sendTransacEmail(sendSmtpEmail);
            log.info("Verification email successfully sent to: {} via Brevo", toEmail);
        } catch (ApiException e) {
            String errorBody = e.getResponseBody() != null ? e.getResponseBody() : "No response body";
            log.error("Brevo API error sending to {}: {} (code: {})", toEmail, errorBody, e.getCode(), e);
            throw new RuntimeException("Не вдалося відправити email через Brevo: " + errorBody, e);
        } catch (Exception e) {
            log.error("Unexpected error sending email to {}", toEmail, e);
            throw new RuntimeException("Не вдалося відправити email: " + e.getMessage(), e);
        }
    }
}