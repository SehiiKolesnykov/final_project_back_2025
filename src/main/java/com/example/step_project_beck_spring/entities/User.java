package com.example.step_project_beck_spring.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Основна сутність користувача.
 * Реалізує UserDetails — тому Spring Security може використовувати її напряму для майбутньої автентифікації.
 */
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_email", columnList = "email", unique = true) // Прискорює пошук по email
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@ToString(exclude = {"password", "posts", "following", "followers", "likes", "savedPosts"}) // Не виводимо пароль і важкі колекції в логах
@NullMarked
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;                         // ID користувача

    @Column(nullable = false, length = 50)
    private String firstName;                // Ім'я користувача

    @Column(nullable = false, length = 50)
    private String lastName;                 // Призвище користувача

    @Column(nullable = false, unique = true, length = 100)
    private String email;                    // Логін = email (як у сучасних соцмережах)

    @Column(nullable = false)
    private String password;                 // Хешований BCrypt

    private LocalDate birthDate;             // Дата народження користувача
    private String avatarUrl;                // Фото профілю
    private String backgroundImgUrl;         // Фото обкладинки профілю

    private boolean emailVerified = false;   // Користувач не може увійти, поки не підтвердить email
    private String googleId;                 // Для входу через Google OAuth (поки не реалізуємо! якщо буде час!!!)

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;         // Таймштамп реєстрації

    @UpdateTimestamp
    private LocalDateTime updatedAt;         // Таймштамп

    // ======================= ВІДНОШЕННЯ =======================

    /** Пости, які створив цей користувач */
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    private List<Post> posts = new ArrayList<>();

    /** На кого підписався цей користувач (я є follower) */
    @OneToMany(mappedBy = "follower", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Follow> following = new HashSet<>();

    /** Хто підписався на цього користувача (я є following) */
    @OneToMany(mappedBy = "following", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Follow> followers = new HashSet<>();

    /** Лайки, які поставив цей користувач */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Like> likes = new HashSet<>();

    /** Пості, які користувач зберіг у "Обране" */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SavedPost> savedPosts = new HashSet<>();

    /** Сповіщення, які отримав користувач */
    @OneToMany(mappedBy = "recipient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notification> notifications = new ArrayList<>();

    /** Кому цей користувач увімкнув сповіщення про свої нові пости */
    @OneToMany(mappedBy = "targetUser", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<NotificationSubscription> notifiedByUsers = new HashSet<>();

    /** На кого цей користувач підписався отримувати сповіщення */
    @OneToMany(mappedBy = "subscriber", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<NotificationSubscription> notifyingUsers = new HashSet<>();

    // ======================= Spring Security =======================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(); // Поки без ролей. Якщо буде час та бажання!!!
    }

    @Override public String getUsername() { return email; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }

    /** Важливо! Користувач може увійти тільки після підтвердження email (по завданню) */
    @Override
    public boolean isEnabled() {
        return emailVerified;
    }
}
