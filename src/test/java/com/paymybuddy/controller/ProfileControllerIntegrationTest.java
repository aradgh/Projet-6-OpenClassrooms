package com.paymybuddy.controller;

import com.paymybuddy.model.User;
import com.paymybuddy.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithMockUser(username = "testuser@example.com", roles = "USER")
class ProfileControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // On vide la table pour isoler chaque test
        userRepository.deleteAll();

        // Création d'un utilisateur de test (celui authentifié)
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("testuser@example.com");
        testUser.setPassword("password"); // Pour les tests, ce n'est pas critique
        testUser = userRepository.save(testUser);
    }

    @Test
    void testShowProfile() throws Exception {
        mockMvc.perform(get("/profile"))
            .andExpect(status().isOk())
            .andExpect(view().name("profile"))
            .andExpect(model().attributeExists("user"))
            .andExpect(model().attribute("user",
                org.hamcrest.Matchers.hasProperty("email", equalTo("testuser@example.com"))));
    }

    @Test
    void testShowEditProfile() throws Exception {
        mockMvc.perform(get("/profile/edit"))
            .andExpect(status().isOk())
            .andExpect(view().name("profile-edit"))
            .andExpect(model().attributeExists("user"))
            .andExpect(model().attribute("user",
                org.hamcrest.Matchers.hasProperty("username", equalTo("testuser"))));
    }

    @Test
    void testUpdateProfile_Success() throws Exception {
        String newUsername = "updatedUser";
        String newEmail = "updated@example.com";
        String newPassword = "newpassword";

        mockMvc.perform(post("/profile/edit")
                .param("username", newUsername)
                .param("email", newEmail)
                .param("password", newPassword))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/profile"));

        // Vérifier que l'utilisateur a bien été mis à jour en base
        User updatedUser = userRepository.findById(testUser.getId()).get();
        assertEquals(newUsername, updatedUser.getUsername());
        assertEquals(newEmail, updatedUser.getEmail());
    }
}
