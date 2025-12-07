package com.example.step_project_beck_spring.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Сутність посту.
 * Містить текст, опціональну картинку, підтримку репостів, цитат, відповідей.
 */
@Entity
@Table(
        name = "posts",
        indexes = {
                // Для швидкого завантаження стрічки: пости авторів, відсортовані за датою
                @Index(name = "idx_post_author_created", columnList = "author_id, createdAt DESC"),
                // Для швидкого пошуку репостів/цитат
                @Index(name = "idx_post_reposted", columnList = "reposted_post_id"),
                @Index(name = "idx_post_quoted", columnList = "quoted_post_id"),
                @Index(name = "idx_post_reply_to", columnList = "reply_to_post_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Текст посту. Обов'язковий, максимум 280 символів */
    @Column(nullable = false, length = 280)
    private String content;

    /** URL картинки. Опціонально — може бути null (картинки немає) */
    @Column(name = "image_url", nullable = true)
    private String imageUrl;

    /** Автор посту — обов'язкове поле */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false, updatable = false)
    private User author;

    // ======================= ТИПИ ПОСТІВ: репости, цитати, відповіді =======================

    /** Якщо це репост — посилання на оригінальний пост */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reposted_post_id")
    private Post repostedPost;

    /** Якщо це цитата (quote tweet) — пост, який цитується */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quoted_post_id")
    private Post quotedPost;

    /** Якщо це відповідь у треді — на який пост відповідає */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_post_id")
    private Post replyToPost;

    // ======================= ЧАС СТВОРЕННЯ =======================

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ======================= ЛІЧИЛЬНИКИ =======================
    // Замість .size() на колекціях — окремі поля.
    // Уникаємо N+1 запитів.

    @Column(name = "likes_count", nullable = false, columnDefinition = "int default 0")
    private int likesCount = 0;

    @Column(name = "comments_count", nullable = false, columnDefinition = "int default 0")
    private int commentsCount = 0;

    @Column(name = "reposts_count", nullable = false, columnDefinition = "int default 0")
    private int repostsCount = 0;

    @Column(name = "quotes_count", nullable = false, columnDefinition = "int default 0")
    private int quotesCount = 0;

    // ======================= ЗВОРОТНІ ЗВ'ЯЗКИ (для навігації та каскадування) =======================

    /** Хто лайкнув цей пост*/
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Like> likes = new HashSet<>();

    /** Хто зберіг цей пост у "Обране" */
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SavedPost> savedBy = new HashSet<>();

    /** Коментарі до цього посту */
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private Set<Comment> comments = new HashSet<>();

    /** Хто відповів на цей пост */
    @OneToMany(mappedBy = "replyToPost", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Post> replies = new HashSet<>();

    /** Хто зробив репост цього посту */
    @OneToMany(mappedBy = "repostedPost", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Post> reposts = new HashSet<>();

    /** Хто процитував цей пост */
    @OneToMany(mappedBy = "quotedPost", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Post> quotes = new HashSet<>();

}