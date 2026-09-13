package com.ecommerce.app.service;

import com.ecommerce.app.dto.AuthResponse;
import com.ecommerce.app.dto.LoginRequest;
import com.ecommerce.app.dto.RegisterRequest;
import com.ecommerce.app.model.User;
import com.ecommerce.app.repository.UserRepository;
import com.ecommerce.app.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setRoles(Set.of("ROLE_USER"));

        User saved = userRepository.save(user);
        String token = jwtUtil.generateToken(saved.getEmail());

        return new AuthResponse(token, saved.getId(), saved.getName(), saved.getEmail(), saved.getRoles());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRoles());
    }
}
