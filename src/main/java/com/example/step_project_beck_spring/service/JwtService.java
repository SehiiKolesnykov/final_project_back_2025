// src/main/java/com/example/step_project_beck_spring/service/JwtService.java
package com.example.step_project_beck_spring.service;

import com.example.step_project_beck_spring.entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    // секретний ключ з application.yml (а там він береться з Render)
    @Value("${jwt.secret}")
    private String secretKey;

    // Це час життя (15 хв)
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    // Це довгий час життя (7 днів)
    @Value("${jwt.long-expiration}")
    private long refreshExpiration;

    /**
     * Головний метод генерації токена.
     * Вибирає час життя залежно від галочки rememberMe.
     */
    public String generateToken(UserDetails userDetails, boolean rememberMe) {
        long expiration = rememberMe ? refreshExpiration : jwtExpiration;
        User user = (User) userDetails; //  щоб взяти ID

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("rememberMe", rememberMe);

        return buildToken(claims, user, expiration);
    }

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername()) // email
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Валідація токена.
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    // Залишив цей метод як дублікат
    public boolean isTokenValid(String token, UserDetails userDetails) {
        return validateToken(token, userDetails);
    }

    /** Витягує username (email) */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}