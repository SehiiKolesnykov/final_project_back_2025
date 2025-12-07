package com.example.step_project_beck_spring.repository;

import com.example.step_project_beck_spring.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

/** JpaRepository для отримання базових CRUD-методів */
public interface UserRepository extends JpaRepository<User, UUID> {

    /** Spring Data JPA автоматично генерує SQL-запит (SELECT FROM users WHERE email = ?) */
    Optional<User> findByEmail(String email);
}