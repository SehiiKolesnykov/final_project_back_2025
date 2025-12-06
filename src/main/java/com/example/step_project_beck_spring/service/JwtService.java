package com.example.step_project_beck_spring.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtService {

    // Секретний ключ для підпису токенів.
    // ? винести у application.yml, щоб не тримати в коді.
    private final String secretKey = "superSecretKeyForJwt";

    // Час життя токена для звичайного входу: 6 годин
    private final long jwtExpiration = 1000 * 60 * 60 * 6;

    // Час життя токена при "залишитися у мережі": 7 днів
    private final long refreshExpiration = 1000L * 60 * 60 * 24 * 7;

    /**
     * Генерує JWT токен для користувача.
     * userDetails — дані користувача (Spring Security UserDetails).
     * rememberMe — якщо true → токен на 7 днів, інакше на 6 годин.
     * @return згенерований JWT токен.
     */
    public String generateToken(UserDetails userDetails, boolean rememberMe) {
        long expiration = rememberMe ? refreshExpiration : jwtExpiration;
        return Jwts.builder()
                .setSubject(userDetails.getUsername()) // email користувача
                .setIssuedAt(new Date())               // час створення токена
                .setExpiration(new Date(System.currentTimeMillis() + expiration)) // час життя
                .signWith(SignatureAlgorithm.HS256, secretKey) // алгоритм підпису
                .compact();
    }

    /**
     * Витягує email із токена.
     */
    public String extractUsername(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * Перевіряє чи email із токена збігається з користувачем
     *  - чи токен ще не прострочений
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /**
     * Перевіряє, чи токен прострочений.
     * @return true, якщо токен уже недійсний.
     */
    private boolean isTokenExpired(String token) {
        Date expiration = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
        return expiration.before(new Date());
    }
}




