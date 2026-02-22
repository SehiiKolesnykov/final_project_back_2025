// com/example/step_project_beck_spring/dto/UserPublicDTO.java
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

    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String avatarUrl;
    private String backgroundImg;
    private String nickName;
    private String aboutMe;

    private int followingCount;
    private int followersCount;
    private int postsCount;

    private boolean following;
}