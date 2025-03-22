package com.paymybuddy.service;

import com.paymybuddy.model.User;
import com.paymybuddy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final AccountService accountService;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, AccountService accountService) {
        this.userRepository = userRepository;
        this.accountService = accountService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User registerUser(String username, String email, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already in use.");
        }
        User user = User.builder()
            .username(username)
            .email(email)
            .password(hashPassword(password))
            .build();

        User savedUser = userRepository.save(user);
        // Créer un compte pour le nouvel utilisateur
        accountService.createAccount(savedUser);
        return userRepository.save(user);
    }


    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getConnections(Long userId) {
        return userRepository.findById(userId).map(User::getConnections)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public void addConnection(User currentUser, User relation) {
        if (currentUser.getConnections() == null) {
            currentUser.setConnections(new ArrayList<>());
        }
        if (!currentUser.getConnections().contains(relation)) {
            currentUser.getConnections().add(relation);
            userRepository.save(currentUser);
        } else {
            throw new IllegalArgumentException("Cette relation existe déjà.");
        }
    }


    private String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return org.springframework.security.core.userdetails.User.builder()
            .username(user.getEmail()) // On utilise l'email pour l'authentification
            .password(user.getPassword())
            .roles("USER")
            .build();
    }

    public User updateProfile(Long userId, String newUsername, String newEmail, String newPassword) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        user.setUsername(newUsername);
        user.setEmail(newEmail);
        // Mettre à jour le mot de passe uniquement s'il a été renseigné
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(newPassword));
        }
        return userRepository.save(user);
    }

}
