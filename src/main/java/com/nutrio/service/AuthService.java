package com.nutrio.service;

import com.nutrio.dto.LoginRequest;
import com.nutrio.dto.RegisterRequest;
import com.nutrio.exception.UserAlreadyExistsException;
import com.nutrio.exception.UserNotFoundException;
import com.nutrio.model.User;
import com.nutrio.repository.UserRepository;
import com.nutrio.security.JwtUtil;
import com.nutrio.dto.UserRegistrationRequest;
import com.nutrio.dto.UpdateProfileRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public String register(RegisterRequest request) {
        logger.debug("Registering new user with email: {}", request.getEmail());
        
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException();
        }

        if (request.getGender() == null) {
            throw new IllegalArgumentException("Пол обязателен для заполнения");
        }
        if (request.getGoal() == null) {
            throw new IllegalArgumentException("Цель обязательна для заполнения");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setAge(request.getAge());
        user.setHeight(request.getHeight());
        user.setWeight(request.getWeight());
        user.setGender(User.Gender.valueOf(request.getGender().toUpperCase()));
        user.setGoal(User.Goal.valueOf(request.getGoal().toUpperCase()));
        user.setAllergies(request.getAllergies());
        
        String activityLevel = request.getActivityLevel();
        if (activityLevel == null || activityLevel.isEmpty()) {
            activityLevel = "MODERATELY_ACTIVE";
        }
        user.setActivityLevel(User.ActivityLevel.valueOf(activityLevel.toUpperCase()));

        userRepository.save(user);
        logger.debug("User registered successfully: {}", user.getEmail());

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                request.getEmail(), null, Collections.emptyList());

        String token = jwtUtil.generateToken(authentication);
        logger.debug("Generated token for user: {}", user.getEmail());
        return token;
    }

    public String login(LoginRequest request) {
        logger.debug("Attempting login for user: {}", request.getEmail());
        
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isEmpty() || !passwordEncoder.matches(request.getPassword(), userOpt.get().getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                request.getEmail(), null, Collections.emptyList());

        String token = jwtUtil.generateToken(authentication);
        logger.debug("Generated token for user: {}", request.getEmail());
        return token;
    }

    public User getUserFromToken(String token) {
        String email = jwtUtil.getUsernameFromToken(token);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public User getCurrentUser(String token) {
        if (!jwtUtil.validateToken(token)) {
            throw new BadCredentialsException("Invalid token");
        }
        return getUserFromToken(token);
    }

    public User createUser(UserRegistrationRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Пользователь с таким email уже существует");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setAge(request.getAge());
        user.setHeight(request.getHeight());
        user.setWeight(request.getWeight());
        user.setGender(User.Gender.valueOf(request.getGender().toUpperCase()));
        user.setGoal(User.Goal.valueOf(request.getGoal().toUpperCase()));
        user.setAllergies(request.getAllergies());

        return userRepository.save(user);
    }

    public User updateProfile(String token, UpdateProfileRequest request) {
        User user = getUserFromToken(token);
        user.setName(request.getName());
        user.setAge(request.getAge());
        user.setHeight(request.getHeight());
        user.setWeight(request.getWeight());
        user.setGender(User.Gender.valueOf(request.getGender().toUpperCase()));
        user.setGoal(User.Goal.valueOf(request.getGoal().toUpperCase()));
        user.setActivityLevel(User.ActivityLevel.valueOf(request.getActivityLevel().toUpperCase()));
        user.setAllergies(request.getAllergies());
        user.setEmail(request.getEmail());
        userRepository.save(user);
        return user;
    }
}