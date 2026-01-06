package com.example.step_project_beck_spring.service;

import com.example.step_project_beck_spring.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurrentUserServiceTest {

    @InjectMocks
    private CurrentUserService currentUserService;

    private User testUser;
    private UUID userId;
    private SecurityContext securityContext;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testUser = new User();
        testUser.setId(userId);
        testUser.setEmail("test@example.com");

        securityContext = mock(SecurityContext.class);
        authentication = mock(Authentication.class);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getCurrentUserId_WhenAuthenticated_ShouldReturnUserId() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);

        // When
        UUID result = currentUserService.getCurrentUserId();

        // Then
        assertNotNull(result);
        assertEquals(userId, result);
    }

    @Test
    void getCurrentUserId_WhenNotAuthenticated_ShouldThrowException() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(null);

        // When & Then
        assertThrows(AccessDeniedException.class, () -> {
            currentUserService.getCurrentUserId();
        });
    }

    @Test
    void getCurrentUserId_WhenPrincipalIsNull_ShouldThrowException() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(null);

        // When & Then
        assertThrows(AccessDeniedException.class, () -> {
            currentUserService.getCurrentUserId();
        });
    }

    @Test
    void getCurrentUserId_WhenPrincipalIsNotUser_ShouldThrowException() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("not a user");

        // When & Then
        assertThrows(AccessDeniedException.class, () -> {
            currentUserService.getCurrentUserId();
        });
    }

    @Test
    void getCurrentUser_WhenAuthenticated_ShouldReturnUser() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);

        // When
        User result = currentUserService.getCurrentUser();

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void getCurrentUser_WhenNotAuthenticated_ShouldThrowException() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(null);

        // When & Then
        assertThrows(AccessDeniedException.class, () -> {
            currentUserService.getCurrentUser();
        });
    }

    @Test
    void getCurrentUser_WhenPrincipalIsNull_ShouldThrowException() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(null);

        // When & Then
        assertThrows(AccessDeniedException.class, () -> {
            currentUserService.getCurrentUser();
        });
    }

    @Test
    void getCurrentUser_WhenPrincipalIsNotUser_ShouldThrowException() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("not a user");

        // When & Then
        assertThrows(AccessDeniedException.class, () -> {
            currentUserService.getCurrentUser();
        });
    }
}

