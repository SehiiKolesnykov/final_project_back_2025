package com.example.step_project_beck_spring.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
@Getter
@Setter
public class UserPrivateDTO extends UserPublicDTO {

    private String email;

    /**LocalDate згідно зі схемою БД */
    private LocalDate birthDate;

    private boolean emailVerified;
}