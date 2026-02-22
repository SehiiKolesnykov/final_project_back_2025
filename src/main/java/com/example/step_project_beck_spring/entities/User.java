package com.example.step_project_beck_spring.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_email", columnList = "email", unique = true),
        @Index(name = "idx_user_firebase_uid", columnList = "firebase_uid", unique = true),
        @Index(name = "idx_user_nick_name", columnList = "nick_name", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"posts", "following", "followers", "likes", "savedPosts", "notifications", "notifiedByUsers", "notifyingUsers"})
@NullMarked
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(nullable = false, length = 50)
    private String lastName;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    private LocalDate birthDate;

    private String avatarUrl;
    private String backgroundImgUrl;

    @NotBlank
    @Size(max = 20)
    @Column(name = "nick_name", nullable = false, unique = true, length = 20)
    private String nickName;

    @Size(max = 160)
    @Column(name = "about_me", length = 160)
    private String aboutMe;

    // Firebase UID — головний ідентифікатор користувача з Firebase
    @Column(name = "firebase_uid", unique = true, nullable = true)
    private String firebaseUid;

    @Column(name = "google_id", unique = true)
    private String googleId;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // ======================= ВІДНОШЕННЯ =======================

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "follower", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Follow> following = new HashSet<>();

    @OneToMany(mappedBy = "following", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Follow> followers = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Like> likes = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SavedPost> savedPosts = new HashSet<>();

    @OneToMany(mappedBy = "recipient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notification> notifications = new ArrayList<>();

    @OneToMany(mappedBy = "targetUser", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<NotificationSubscription> notifiedByUsers = new HashSet<>();

    @OneToMany(mappedBy = "subscriber", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<NotificationSubscription> notifyingUsers = new HashSet<>();

    // ======================= Spring Security methods =======================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(); // без ролей поки що
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // При Firebase Auth зазвичай завжди true, якщо користувач існує
        // (Firebase вже не дозволяє логін, якщо акаунт заблокований/видалений)
        return true;
    }

    @Override
    public String getPassword() {
        return null;
    }
}