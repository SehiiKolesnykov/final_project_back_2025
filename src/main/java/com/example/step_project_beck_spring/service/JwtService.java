package com.example.step_project_beck_spring.service;

import com.example.step_project_beck_spring.entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

/**
 * Сервіс для генерації, парсингу та валідації JWT-токенів.
 *
 * Використовується і фільтром (JwtAuthenticationFilter), і AuthService.
 * Важливо: секретний ключ має бути довгим та випадковим, і в продакшені — в .env / application.yml!
 */
@Service
public class JwtService {

    // Секретний ключ для підпису токенів (HS256).
    // Зараз захардкоджений — це ОК тільки для розробки!
    // У продакшені обов’язково винести в application.yml або змінні оточення!
    private final String secretKey = "8F2Kj9LmNqRtUvWxYzAbCdEfGhIjKlMnOpQrStUvWxYz1234567890abcdef12345678";

    // Термін звичайного токена — 6 годин
    private final long jwtExpiration = 1000 * 60 * 60 * 6;

    // Термін токена при "Запам’ятати мене" — 7 днів
    private final long refreshExpiration = 1000L * 60 * 60 * 24 * 7;

    // Повертає ключ у форматі, який приймає JJWT
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Генерує JWT-токен для користувача.
     *
     * @param userDetails  об’єкт UserDetails (у нас це наш User, бо він implements UserDetails)
     * @param rememberMe   якщо true → токен на 7 днів, false → на 6 годин
     * @return готовий JWT як String
     */
    public String generateToken(UserDetails userDetails, boolean rememberMe) {
        long expiration = rememberMe ? refreshExpiration : jwtExpiration;
        User user = (User) userDetails;  // безпечний каст, бо наш User реалізує UserDetails

        return Jwts.builder()
                .setSubject(user.getEmail())                       // стандартне поле — email як username
                .claim("userId", user.getId().toString())          // кастомне поле — UUID користувача
                .claim("rememberMe", rememberMe)                   // щоб знати, чи був "запам’ятати мене"
                .setIssuedAt(new Date())                           // коли виданий
                .setExpiration(new Date(System.currentTimeMillis() + expiration)) // коли закінчується
                .signWith(SignatureAlgorithm.HS256, secretKey)     // підпис (старий спосіб, але працює в 0.11.5)
                .compact();
    }

    // Універсальний метод для витягування будь-якого claim з токена
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Парсить токен і повертає всі claims (з перевіркою підпису)
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)           // старий метод, бо нова версія JJWT 0.12+ ламає сумісність
                .parseClaimsJws(token)
                .getBody();
    }

    /** Витягує email (email) з токена */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /** Витягує userId (UUID у вигляді String) з токена */
    public String extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", String.class));
    }

    /**
     * Перевіряє валідність токена:
     * - чи збігається email
     * - чи не прострочений
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /** Чи закінчився термін дії токена */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}