package com.paymybuddy.controller;

import com.paymybuddy.model.User;
import com.paymybuddy.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "test@example.com", roles = "USER")
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    // Avant chaque test, on nettoie la base de données
    @BeforeEach
    void setup() {
        userRepository.deleteAll();
    }

    @Test
    void testCreateUser() throws Exception {
        String userJson = "{\"username\":\"testuser\",\"email\":\"test@example.com\",\"password\":\"password\"}";
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username", is("testuser")))
            .andExpect(jsonPath("$.email", is("test@example.com")));
    }

    @Test
    void testGetUserByEmail() throws Exception {
        // Créer un utilisateur dans la base
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password");
        userRepository.save(user);

        // Appel de l'API GET par email
        mockMvc.perform(get("/api/users/email/test@example.com"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username", is("testuser")))
            .andExpect(jsonPath("$.email", is("test@example.com")));
    }

    @Test
    void testGetAllUsers() throws Exception {
        // Créer deux utilisateurs
        User user1 = new User();
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");
        user1.setPassword("password1");

        User user2 = new User();
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setPassword("password2");

        userRepository.save(user1);
        userRepository.save(user2);

        mockMvc.perform(get("/api/users")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[?(@.email=='user1@example.com')].username", contains("user1")))
            .andExpect(jsonPath("$[?(@.email=='user2@example.com')].username", contains("user2")));
    }

    @Test
    void testGetUserById() throws Exception {
        User user = new User();
        user.setUsername("user");
        user.setEmail("user@example.com");
        user.setPassword("password");
        user = userRepository.save(user);

        mockMvc.perform(get("/api/users/{id}", user.getId())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username", is("user")))
            .andExpect(jsonPath("$.email", is("user@example.com")));
    }

    @Test
    void testUpdateUser() throws Exception {
        // Créer un utilisateur initial
        User user = new User();
        user.setUsername("olduser");
        user.setEmail("old@example.com");
        user.setPassword("oldpassword");
        user = userRepository.save(user);

        // Préparer la nouvelle version au format JSON
        String updatedJson = "{\"username\":\"newuser\",\"email\":\"new@example.com\",\"password\":\"newpassword\"}";

        mockMvc.perform(put("/api/users/{id}", user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatedJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username", is("newuser")))
            .andExpect(jsonPath("$.email", is("new@example.com")));
    }

    @Test
    void testDeleteUser() throws Exception {
        User user = new User();
        user.setUsername("deleteuser");
        user.setEmail("delete@example.com");
        user.setPassword("password");
        user = userRepository.save(user);

        mockMvc.perform(delete("/api/users/{id}", user.getId()))
            .andExpect(status().isNoContent());
    }
}
