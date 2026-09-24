package com.hv.meeting_scheduler_api_java.service;

import com.hv.meeting_scheduler_api_java.domain.User;
import com.hv.meeting_scheduler_api_java.dto.AuthResponse;
import com.hv.meeting_scheduler_api_java.dto.LoginRequest;
import com.hv.meeting_scheduler_api_java.dto.SignupRequest;
import com.hv.meeting_scheduler_api_java.repository.UserRepository;
import com.hv.meeting_scheduler_api_java.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse signup(SignupRequest request) {
        userRepository.findByEmail(request.email()).ifPresent(u -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "user already exists");
        });

        String hashedPassword = passwordEncoder.encode(request.password());
        User user = new User(request.name(), request.email(), hashedPassword);
        User saved = userRepository.save(user);

        String token = jwtService.generateToken(saved.getId());
        return new AuthResponse(saved.getId(), saved.getName(), saved.getEmail(), token);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getHashedPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid email or password");
        }

        String token = jwtService.generateToken(user.getId());
        return new AuthResponse(user.getId(), user.getName(), user.getEmail(), token);
    }
}