package com.example.step_project_beck_spring.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserPublicDTO {

    /** Унікальний ідентифікатор користувача */
    private UUID id;

    private String firstName;
    private String lastName;
    private String avatarUrl;
    private String backgroundImg;

    /** (Сounters) не знаю як українською */

    private int followingCount;
    private int followersCount;
    private int postsCount;

    /** Чи підписаний авторизований користувач на цей профіль. Поки що завжди false бо немає механізму авторизації.*/
    private boolean isFollowing;
}