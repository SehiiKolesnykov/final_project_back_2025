package com.example.step_project_beck_spring.service;

import com.example.step_project_beck_spring.dto.UserPublicDTO;
import java.util.UUID;

public interface UserService {

    UserPublicDTO getUserById(UUID id);
    UserPublicDTO getUserByEmail(String email);
}