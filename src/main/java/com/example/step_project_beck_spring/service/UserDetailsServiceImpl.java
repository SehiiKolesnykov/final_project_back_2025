package com.example.step_project_beck_spring.service;

import com.example.step_project_beck_spring.entities.User;
import com.example.step_project_beck_spring.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Сервіс для завантаження користувачів з бази даних для Spring Security.
 * Використовується при логіні через AuthenticationManager.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("=== UserDetailsServiceImpl.loadUserByUsername ===");
        System.out.println("Loading user with email: " + email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    System.err.println("❌ User not found with email: " + email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });
        
        System.out.println("✅ User found: " + user.getEmail());
        System.out.println("User ID: " + user.getId());
        System.out.println("Password hash: " + (user.getPassword() != null ? user.getPassword().substring(0, Math.min(20, user.getPassword().length())) + "..." : "null"));
        System.out.println("User enabled (isEnabled): " + user.isEnabled());
        
        return user; // User реалізує UserDetails, тому повертаємо його напряму
    }
}

