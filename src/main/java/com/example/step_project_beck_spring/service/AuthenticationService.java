package com.example.step_project_beck_spring.service;

import com.example.step_project_beck_spring.request.AuthResponse;
import com.example.step_project_beck_spring.request.LoginRequest;
import com.example.step_project_beck_spring.request.RegisterRequest;
import com.example.step_project_beck_spring.entities.User;
import com.example.step_project_beck_spring.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RestTemplate restTemplate;

    @Value("${firebase.web-api-key}")
    private String firebaseWebApiKey;

    /** РЕЄСТРАЦІЯ */
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("Email already taken / Цей email вже зайнятий");
        }

        String nickName = request.nickName();
        if (nickName == null || nickName.isBlank()) {
            String emailPart = request.email().substring(0, request.email().indexOf('@'));
            nickName = emailPart.length() > 19 ? emailPart.substring(0, 19) : emailPart;
        }

        // Check uniqueness
        if (userRepository.existsByNickName(nickName)) {
            throw new RuntimeException("Nickname already taken");
        }

        UserRecord.CreateRequest createRequest = new UserRecord.CreateRequest()
                .setEmail(request.email())
                .setPassword(request.password())
                .setDisplayName(request.firstName() + " " + request.lastName())
                .setEmailVerified(true); // Без перевірки email

        UserRecord firebaseUser;
        try {
            firebaseUser = FirebaseAuth.getInstance().createUser(createRequest);
        } catch (FirebaseAuthException e) {
            throw new RuntimeException("Failed to create user in Firebase: " + e.getMessage());
        }

        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .birthDate(request.birthDate())
                .nickName(nickName)
                .firebaseUid(firebaseUser.getUid())
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user, false);
        return new AuthResponse(token);
    }

    /** ЛОГІН */
    public AuthResponse login(LoginRequest request) {
        // Використовуємо REST API для signInWithPassword
        String url = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + firebaseWebApiKey;

        Map<String, Object> body = new HashMap<>();
        body.put("email", request.getEmail());
        body.put("password", request.getPassword());
        body.put("returnSecureToken", true);

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.postForObject(url, body, Map.class);

        if (response == null || response.containsKey("error")) {
            throw new RuntimeException("Invalid email or password / Невірний email або пароль");
        }

        String uid = (String) response.get("localId");

        User user = userRepository.findByFirebaseUid(uid)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtService.generateToken(user, request.isRememberMe());
        return new AuthResponse(token);
    }
}