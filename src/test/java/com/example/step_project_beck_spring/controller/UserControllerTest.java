//package com.example.step_project_beck_spring.controller;
//
//import com.example.step_project_beck_spring.dto.UserPrivateDTO;
//import com.example.step_project_beck_spring.dto.UserPublicDTO;
//import com.example.step_project_beck_spring.entities.User;
//import com.example.step_project_beck_spring.repository.UserRepository;
//import com.example.step_project_beck_spring.service.CurrentUserService;
//import com.example.step_project_beck_spring.service.UserService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//
//import java.time.LocalDate;
//import java.util.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class UserControllerTest {
//
//    @Mock
//    private UserService userService;
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private CurrentUserService currentUserService;
//
//    @InjectMocks
//    private UserController userController;
//
//    private User testUser;
//    private UUID userId;
//
//    @BeforeEach
//    void setUp() {
//        userId = UUID.randomUUID();
//        testUser = new User();
//        testUser.setId(userId);
//        testUser.setEmail("test@example.com");
//        testUser.setFirstName("John");
//        testUser.setLastName("Doe");
//        testUser.setBirthDate(LocalDate.of(1990, 1, 1));
//        testUser.setEmailVerified(true);
//        testUser.setAvatarUrl("avatar.jpg");
//        testUser.setBackgroundImgUrl("background.jpg");
//        testUser.setPosts(new ArrayList<>());
//        testUser.setFollowers(new HashSet<>());
//        testUser.setFollowing(new HashSet<>());
//    }
//
//    @Test
//    void getAllUsers_ShouldReturnListOfUsers() {
//        // Given
//        List<User> users = List.of(testUser);
//        when(userRepository.findAll()).thenReturn(users);
//
//        // When
//        ResponseEntity<List<UserPublicDTO>> response = userController.getAllUsers();
//
//        // Then
//        assertNotNull(response);
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertNotNull(response.getBody());
//        assertEquals(1, response.getBody().size());
//        assertEquals(userId, response.getBody().get(0).getId());
//        verify(userRepository, times(1)).findAll();
//    }
//
//    @Test
//    void getUserById_WhenUserExists_ShouldReturnUser() {
//        // Given
//        UserPublicDTO userDTO = new UserPublicDTO();
//        userDTO.setId(userId);
//        when(userService.getUserById(userId)).thenReturn(userDTO);
//
//        // When
//        ResponseEntity<UserPublicDTO> response = userController.getUserById(userId);
//
//        // Then
//        assertNotNull(response);
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertNotNull(response.getBody());
//        assertEquals(userId, response.getBody().getId());
//        verify(userService, times(1)).getUserById(userId);
//    }
//
//    @Test
//    void getUserById_WhenUserNotFound_ShouldReturnNotFound() {
//        // Given
//        when(userService.getUserById(userId))
//                .thenThrow(new NoSuchElementException("User not found"));
//
//        // When
//        ResponseEntity<UserPublicDTO> response = userController.getUserById(userId);
//
//        // Then
//        assertNotNull(response);
//        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
//        assertNull(response.getBody());
//    }
//
//    @Test
//    void getUserByEmail_WhenUserExists_ShouldReturnUser() {
//        // Given
//        String email = "test@example.com";
//        UserPublicDTO userDTO = new UserPublicDTO();
//        userDTO.setId(userId);
//        when(userService.getUserByEmail(email)).thenReturn(userDTO);
//
//        // When
//        ResponseEntity<UserPublicDTO> response = userController.getUserByEmail(email);
//
//        // Then
//        assertNotNull(response);
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertNotNull(response.getBody());
//        assertEquals(userId, response.getBody().getId());
//        verify(userService, times(1)).getUserByEmail(email);
//    }
//
//    @Test
//    void getUserByEmail_WhenUserNotFound_ShouldReturnNotFound() {
//        // Given
//        String email = "notfound@example.com";
//        when(userService.getUserByEmail(email))
//                .thenThrow(new NoSuchElementException("User not found"));
//
//        // When
//        ResponseEntity<UserPublicDTO> response = userController.getUserByEmail(email);
//
//        // Then
//        assertNotNull(response);
//        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
//    }
//
//    @Test
//    void getCurrentUser_WhenAuthenticated_ShouldReturnUser() {
//        // Given
//        when(currentUserService.getCurrentUser()).thenReturn(testUser);
//
//        // When
//        ResponseEntity<UserPrivateDTO> response = userController.getCurrentUser();
//
//        // Then
//        assertNotNull(response);
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertNotNull(response.getBody());
//        assertEquals(userId, response.getBody().getId());
//        assertEquals("test@example.com", response.getBody().getEmail());
//        assertEquals("John", response.getBody().getFirstName());
//        assertEquals("Doe", response.getBody().getLastName());
//        verify(currentUserService, times(1)).getCurrentUser();
//    }
//
//    @Test
//    void getCurrentUser_WhenNotAuthenticated_ShouldReturnUnauthorized() {
//        // Given
//        when(currentUserService.getCurrentUser())
//                .thenThrow(new RuntimeException("Not authenticated"));
//
//        // When
//        ResponseEntity<UserPrivateDTO> response = userController.getCurrentUser();
//
//        // Then
//        assertNotNull(response);
//        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
//    }
//
//    @Test
//    void updateCurrentUser_ShouldUpdateUserSuccessfully() {
//        // Given
//        Map<String, Object> userData = new HashMap<>();
//        userData.put("firstName", "Jane");
//        userData.put("lastName", "Smith");
//        userData.put("avatarUrl", "new-avatar.jpg");
//
//        when(currentUserService.getCurrentUser()).thenReturn(testUser);
//        when(userRepository.save(any(User.class))).thenReturn(testUser);
//
//        // When
//        ResponseEntity<UserPrivateDTO> response = userController.updateCurrentUser(userData);
//
//        // Then
//        assertNotNull(response);
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertNotNull(response.getBody());
//        verify(userRepository, times(1)).save(any(User.class));
//    }
//
//    @Test
//    void updateCurrentUser_WithEmail_ShouldUpdateEmail() {
//        // Given
//        Map<String, Object> userData = new HashMap<>();
//        userData.put("email", "newemail@example.com");
//
//        when(currentUserService.getCurrentUser()).thenReturn(testUser);
//        when(userRepository.findByEmail("newemail@example.com"))
//                .thenReturn(Optional.empty());
//        when(userRepository.save(any(User.class))).thenReturn(testUser);
//
//        // When
//        ResponseEntity<UserPrivateDTO> response = userController.updateCurrentUser(userData);
//
//        // Then
//        assertNotNull(response);
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        verify(userRepository, times(1)).findByEmail("newemail@example.com");
//        verify(userRepository, times(1)).save(any(User.class));
//    }
//
//    @Test
//    void updateCurrentUser_WithExistingEmail_ShouldReturnBadRequest() {
//        // Given
//        User otherUser = new User();
//        otherUser.setId(UUID.randomUUID());
//        Map<String, Object> userData = new HashMap<>();
//        userData.put("email", "existing@example.com");
//
//        when(currentUserService.getCurrentUser()).thenReturn(testUser);
//        when(userRepository.findByEmail("existing@example.com"))
//                .thenReturn(Optional.of(otherUser));
//
//        // When
//        ResponseEntity<UserPrivateDTO> response = userController.updateCurrentUser(userData);
//
//        // Then
//        assertNotNull(response);
//        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
//        verify(userRepository, never()).save(any(User.class));
//    }
//
//    @Test
//    void updateCurrentUser_WithBirthDateAsString_ShouldParseAndUpdate() {
//        // Given
//        Map<String, Object> userData = new HashMap<>();
//        userData.put("birthDate", "1995-05-15");
//
//        when(currentUserService.getCurrentUser()).thenReturn(testUser);
//        when(userRepository.save(any(User.class))).thenReturn(testUser);
//
//        // When
//        ResponseEntity<UserPrivateDTO> response = userController.updateCurrentUser(userData);
//
//        // Then
//        assertNotNull(response);
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        verify(userRepository, times(1)).save(any(User.class));
//    }
//
//    @Test
//    void updateCurrentUser_WhenNotAuthenticated_ShouldReturnUnauthorized() {
//        // Given
//        Map<String, Object> userData = new HashMap<>();
//        when(currentUserService.getCurrentUser())
//                .thenThrow(new RuntimeException("Not authenticated"));
//
//        // When
//        ResponseEntity<UserPrivateDTO> response = userController.updateCurrentUser(userData);
//
//        // Then
//        assertNotNull(response);
//        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
//    }
//
//    @Test
//    void deleteUser_WhenUserExists_ShouldDeleteUser() {
//        // Given
//        when(userRepository.existsById(userId)).thenReturn(true);
//        doNothing().when(userRepository).deleteById(userId);
//
//        // When
//        ResponseEntity<Void> response = userController.deleteUser(userId);
//
//        // Then
//        assertNotNull(response);
//        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
//        verify(userRepository, times(1)).existsById(userId);
//        verify(userRepository, times(1)).deleteById(userId);
//    }
//
//    @Test
//    void deleteUser_WhenUserNotExists_ShouldReturnNotFound() {
//        // Given
//        when(userRepository.existsById(userId)).thenReturn(false);
//
//        // When
//        ResponseEntity<Void> response = userController.deleteUser(userId);
//
//        // Then
//        assertNotNull(response);
//        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
//        verify(userRepository, times(1)).existsById(userId);
//        verify(userRepository, never()).deleteById(any());
//    }
//}
//
