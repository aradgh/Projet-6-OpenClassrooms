package com.paymybuddy.controller;

import com.paymybuddy.model.User;
import com.paymybuddy.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RegisterControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setup() {
        // Nettoyage de la base avant chaque test
        userRepository.deleteAll();
    }

    @Test
    void testShowRegisterPage() throws Exception {
        mockMvc.perform(get("/register"))
            .andExpect(status().isOk())
            .andExpect(view().name("register"));
    }

    @Test
    void testRegisterUser_Success() throws Exception {
        mockMvc.perform(post("/register")
                .param("username", "testuser")
                .param("email", "testuser@example.com")
                .param("password", "password"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));

        Optional<User> userOpt = userRepository.findByEmail("testuser@example.com");
        assertTrue(userOpt.isPresent(), "L'utilisateur doit être enregistré");
        assertEquals("testuser", userOpt.get().getUsername());
    }

    @Test
    void testRegisterUser_Error_DuplicateEmail() throws Exception {
        // Créer un utilisateur existant avec l'email en doublon
        User existingUser = new User();
        existingUser.setUsername("existing");
        existingUser.setEmail("duplicate@example.com");
        existingUser.setPassword("password");
        userRepository.save(existingUser);

        mockMvc.perform(post("/register")
                .param("username", "newuser")
                .param("email", "duplicate@example.com")
                .param("password", "newpassword"))
            .andExpect(status().isOk())
            .andExpect(view().name("register"))
            .andExpect(model().attributeExists("error"))
            .andExpect(model().attribute("error", containsString("Email already in use")));
    }
}
