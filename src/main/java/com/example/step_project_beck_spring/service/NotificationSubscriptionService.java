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

    // ──── NEW_POST ──── (залишаємо як було)
    @Transactional
    public void notifySubscribersAboutNewPost(Post post) {
        List<NotificationSubscription> subscribers =
                subscriptionRepository.findByTargetUserId(post.getAuthor().getId());
        for (NotificationSubscription sub : subscribers) {
            if (sub.isNotifyOnAllPosts()) {
                notificationService.createNotification(
                        sub.getSubscriber(),
                        post.getAuthor(),
                        NotificationType.NEW_POST,
                        post.getId(),
                        post.getAuthor().getNickName() + " опублікував(-ла) новий пост",
                        "/posts/" + post.getId()
                );
            }
        }
    }

    // ──── LIKE ────
    @Transactional
    public void notifyAboutLike(Post post, User liker) {
        User author = post.getAuthor();
        if (author.equals(liker)) return; // не сповіщаємо самого себе

        List<NotificationSubscription> subs = subscriptionRepository.findByTargetUserId(author.getId());
        for (NotificationSubscription sub : subs) {
            notificationService.createNotification(
                    sub.getSubscriber(),
                    liker,
                    NotificationType.LIKE,
                    post.getId(),
                    liker.getNickName() + " поставив лайк під твоїм постом",
                    "/posts/" + post.getId()
            );
        }
    }

    // ──── COMMENT ────
    @Transactional
    public void notifyAboutComment(Post post, User commenter, Comment comment) {
        User author = post.getAuthor();
        if (author.equals(commenter)) return;

        List<NotificationSubscription> subs = subscriptionRepository.findByTargetUserId(author.getId());
        for (NotificationSubscription sub : subs) {
            notificationService.createNotification(
                    sub.getSubscriber(),
                    commenter,
                    NotificationType.COMMENT,
                    post.getId(),
                    commenter.getNickName() + " залишив коментар під твоїм постом",
                    "/posts/" + post.getId()
            );
        }
    }

    // ──── FOLLOW ────
    @Transactional
    public void notifyAboutFollow(User followed, User follower) {
        if (followed.equals(follower)) return;

        notificationService.createNotification(
                followed,
                follower,
                NotificationType.FOLLOW,
                follower.getId(),
                follower.getNickName() + " підписався на тебе",
                "/profile/" + follower.getId()
        );
    }

    // ──── MESSAGE ──── (для чату — сповіщаємо отримувача)
    @Transactional
    public void notifyAboutMessage(ChatMessage message) {
        User sender = message.getSender();
        ChatThread thread = message.getThread();

        // Знаходимо отримувача (той, хто не sender)
        User recipient = thread.getParticipants().stream()
                .filter(u -> !u.getId().equals(sender.getId()))
                .findFirst()
                .orElse(null);

        if (recipient == null) return;

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