package com.example.step_project_beck_spring.service;

import com.example.step_project_beck_spring.entities.NotificationSubscription;
import com.example.step_project_beck_spring.entities.Post;
import com.example.step_project_beck_spring.entities.User;
import com.example.step_project_beck_spring.repository.NotificationSubscriptionRepository;
import com.example.step_project_beck_spring.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

//Сервіс для керування підписками на сповіщення про нові пости.
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

    //Викликається при створенні нового поста. Розсилає сповіщення всім підписникам автора.
    @Transactional
    public void notifySubscribersAboutNewPost(Post post) {
        List<NotificationSubscription> subscribers =
                subscriptionRepository.findByTargetUserId(post.getAuthor().getId());
        for (NotificationSubscription subscription : subscribers) {
            if (subscription.isNotifyOnAllPosts()) {
                notificationService.createNewPostNotification(subscription.getSubscriber(), post);
            }
        }
    }
}



