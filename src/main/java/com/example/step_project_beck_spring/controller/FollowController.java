package com.example.step_project_beck_spring.controller;

import com.example.step_project_beck_spring.dto.UserSummaryDto;
import com.example.step_project_beck_spring.entities.User;
import com.example.step_project_beck_spring.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/follow")
@RequiredArgsConstructor
public class FollowController {
    private final FollowService followService;

    //Підписатися на користувача.
    @PostMapping("/{targetUserId}")
    public ResponseEntity<Void> follow(@PathVariable UUID targetUserId,
                                       Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        followService.follow(currentUser.getId(), targetUserId);
        return ResponseEntity.ok().build();
    }

    //Відписатися від користувача.
    @DeleteMapping("/{targetUserId}")
    public ResponseEntity<Void> unfollow(@PathVariable UUID targetUserId,
                                         Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        followService.unfollow(currentUser.getId(), targetUserId);
        return ResponseEntity.noContent().build();
    }

    //Перевірка, чи поточний користувач підписаний на targetUserId.
     // Для кнопки Follow / Unfollow на фронті.
    @GetMapping("/status/{targetUserId}")
    public ResponseEntity<Boolean> isFollowing(@PathVariable UUID targetUserId,
                                               Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        boolean following = followService.isFollowing(currentUser.getId(), targetUserId);
        return ResponseEntity.ok(following);
    }
    //Список користувачів, на яких підписаний поточний користувач (following).
    @GetMapping("/me/following")
    public ResponseEntity<List<UserSummaryDto>> getMyFollowing(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        List<UserSummaryDto> result = followService.getFollowing(currentUser.getId());
        return ResponseEntity.ok(result);
    }

    //Список користувачів, які підписані на поточного користувача (followers).
    @GetMapping("/me/followers")
    public ResponseEntity<List<UserSummaryDto>> getMyFollowers(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        List<UserSummaryDto> result = followService.getFollowers(currentUser.getId());
        return ResponseEntity.ok(result);
    }

    //Список користувачів, на яких підписаний userId (профіль іншого користувача).
    @GetMapping("/{userId}/following")
    public ResponseEntity<List<UserSummaryDto>> getUserFollowing(@PathVariable UUID userId) {
        List<UserSummaryDto> result = followService.getFollowing(userId);
        return ResponseEntity.ok(result);
    }

    //Список користувачів, які підписані на userId (профіль іншого користувача).
    @GetMapping("/{userId}/followers")
    public ResponseEntity<List<UserSummaryDto>> getUserFollowers(@PathVariable UUID userId) {
        List<UserSummaryDto> result = followService.getFollowers(userId);
        return ResponseEntity.ok(result);
    }

    //Кількість фоловерів користувача.
     //Можна використовувати для відображення на сторінці профілю.
    @GetMapping("/{userId}/followers/count")
    public ResponseEntity<Long> getFollowersCount(@PathVariable UUID userId) {
        long count = followService.countFollowers(userId);
        return ResponseEntity.ok(count);
    }

    //Кількість фолловінгів користувача.
    @GetMapping("/{userId}/following/count")
    public ResponseEntity<Long> getFollowingCount(@PathVariable UUID userId) {
        long count = followService.countFollowing(userId);
        return ResponseEntity.ok(count);
    }
}


