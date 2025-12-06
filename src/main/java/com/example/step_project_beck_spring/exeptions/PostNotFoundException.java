package com.example.step_project_beck_spring.exeptions;

import java.util.UUID;

public class PostNotFoundException extends RuntimeException {
    public PostNotFoundException(UUID id) {
        super("Post with id " + id + " not found");
    }
}



