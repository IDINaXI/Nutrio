package com.nutrio.service;

import com.nutrio.model.User;
import java.util.Optional;

public interface UserService {
    Optional<User> findByEmail(String email);
    User save(User user);
    User getUserById(Long userId);
} 