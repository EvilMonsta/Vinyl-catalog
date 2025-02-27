package com.example.vinyltrackerapi.service;

import com.example.vinyltrackerapi.api.models.User;
import com.example.vinyltrackerapi.api.repositories.UserRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUser(Integer id) {
        return userRepository.findById(id);
    }

    public List<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Пользователь с таким email уже существует!");
        }
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Пользователь с таким email уже существует!");
        }
        return userRepository.save(user);
    }

    public User updateUser(Integer id, User newUserData) {
        return userRepository.findById(id).map(user -> {
            user.setUsername(newUserData.getUsername());
            user.setEmail(newUserData.getEmail());
            user.setPassword(newUserData.getPassword());
            user.setRole(newUserData.getRole());
            return userRepository.save(user);
        }).orElseThrow(() -> new RuntimeException("Пользователь не найден!"));
    }

    public void deleteUser(Integer id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Пользователь с ID " + id + " не найден!");
        }
        userRepository.deleteById(id);
    }
}
