package com.example.step_project_beck_spring.service;

import com.example.step_project_beck_spring.entities.*;
import com.example.step_project_beck_spring.enums.NotificationType;
import com.example.step_project_beck_spring.repository.NotificationSubscriptionRepository;
import com.example.step_project_beck_spring.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationSubscriptionService {

    private final NotificationSubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public void subscribe(UUID subscriberId, UUID targetUserId) {
        if (subscriberId.equals(targetUserId)) {
            throw new IllegalArgumentException("Не можна підписатися на сповіщення про самого себе");
        }
        if (subscriptionRepository.existsBySubscriberIdAndTargetUserId(subscriberId, targetUserId)) {
            return;
        }
        User subscriber = userRepository.findById(subscriberId)
                .orElseThrow(() -> new IllegalArgumentException("Subscriber не знайдений"));
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("Target user не знайдений"));
        NotificationSubscription subscription = NotificationSubscription.builder()
                .subscriber(subscriber)
                .targetUser(targetUser)
                .notifyOnAllPosts(true)
                .build();
        subscriptionRepository.save(subscription);
    }

    @Transactional
    public void unsubscribe(UUID subscriberId, UUID targetUserId) {
        subscriptionRepository.deleteBySubscriberIdAndTargetUserId(subscriberId, targetUserId);
    }

    // ──── NEW_POST ──── (сповіщаємо підписників про новий пост)
    @Transactional
    public void notifySubscribersAboutNewPost(Post post) {
        User author = post.getAuthor();
        List<NotificationSubscription> subscribers = subscriptionRepository.findByTargetUserId(author.getId());

        for (NotificationSubscription sub : subscribers) {
            User recipient = sub.getSubscriber();
            if (recipient.equals(author)) {
                continue; // не сповіщаємо автора про свій пост
            }

            notificationService.createNotification(
                    recipient,
                    author,
                    NotificationType.NEW_POST,
                    post.getId(),
                    author.getNickName() + " опублікував(-ла) новий пост",
                    "/posts/" + post.getId()
            );
        }
    }

    // ──── LIKE ──── (тільки автору поста)
    @Transactional
    public void notifyAboutLike(Post post, User liker) {
        User author = post.getAuthor();

        // Не сповіщаємо автора про свій власний лайк
        if (author.equals(liker)) {
            return;
        }

        // Сповіщення надсилається ТІЛЬКИ автору поста
        notificationService.createNotification(
                author,               // отримувач — тільки автор поста
                liker,
                NotificationType.LIKE,
                post.getId(),
                liker.getNickName() + " поставив лайк під твоїм постом",
                "/posts/" + post.getId()
        );
    }

    // ──── COMMENT ──── (тільки автору поста)
    @Transactional
    public void notifyAboutComment(Post post, User commenter, Comment comment) {
        User author = post.getAuthor();

        // Не сповіщаємо автора про свій власний коментар
        if (author.equals(commenter)) {
            return;
        }

        // Сповіщення надсилається ТІЛЬКИ автору поста
        notificationService.createNotification(
                author,               // отримувач — тільки автор поста
                commenter,
                NotificationType.COMMENT,
                post.getId(),
                commenter.getNickName() + " залишив коментар під твоїм постом",
                "/posts/" + post.getId()
        );
    }

    // ──── FOLLOW ──── (тільки тому, на кого підписалися)
    @Transactional
    public void notifyAboutFollow(User followed, User follower) {
        if (followed.equals(follower)) {
            return;
        }

        notificationService.createNotification(
                followed,
                follower,
                NotificationType.FOLLOW,
                follower.getId(),
                follower.getNickName() + " підписався на тебе",
                "/profile/" + follower.getId()
        );
    }

    // ──── MESSAGE ──── (тільки отримувачу)
    @Transactional
    public void notifyAboutMessage(ChatMessage message) {
        User sender = message.getSender();
        ChatThread thread = message.getThread();

        User recipient = thread.getParticipants().stream()
                .filter(u -> !u.getId().equals(sender.getId()))
                .findFirst()
                .orElse(null);

        if (recipient == null) {
            return;
        }

        notificationService.createNotification(
                recipient,
                sender,
                NotificationType.MESSAGE,
                message.getId(),
                sender.getNickName() + " надіслав тобі повідомлення",
                "/chat/" + thread.getId()
        );
    }
}