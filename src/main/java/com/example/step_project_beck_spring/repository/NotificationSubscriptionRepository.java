package com.example.step_project_beck_spring.repository;

import com.example.step_project_beck_spring.entities.NotificationSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

//Репозиторій для підписок на сповіщення про нові пости.

public interface NotificationSubscriptionRepository extends JpaRepository<NotificationSubscription, UUID> {

    boolean existsBySubscriberIdAndTargetUserId(UUID subscriberId, UUID targetUserId);

    void deleteBySubscriberIdAndTargetUserId(UUID subscriberId, UUID targetUserId);

    List<NotificationSubscription> findBySubscriberId(UUID subscriberId);

    List<NotificationSubscription> findByTargetUserId(UUID targetUserId);
}


