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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "testuser@example.com", roles = "USER")
@Transactional // <-- Pour que la session reste ouverte pendant le test
class AddRelationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private User relationUser;

    @BeforeEach
    public void setup() {
        // Nettoyage de la base pour éviter les interférences
        userRepository.deleteAll();

        // Création d'un utilisateur de test (celui connecté)
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("testuser@example.com");
        testUser.setPassword("password"); // Pour le test, le hachage n'est pas nécessaire
        testUser = userRepository.save(testUser);

        // Création d'un autre utilisateur qui pourra être ajouté en relation
        relationUser = new User();
        relationUser.setUsername("otheruser");
        relationUser.setEmail("otheruser@example.com");
        relationUser.setPassword("password");
        relationUser = userRepository.save(relationUser);
    }

    @Test
    void testShowAddRelationPage() throws Exception {
        mockMvc.perform(get("/add-relation"))
            .andExpect(status().isOk())
            .andExpect(view().name("add-relation"));
    }

    @Test
    void testAddRelation_Success() throws Exception {
        mockMvc.perform(post("/add-relation")
                .param("email", "otheruser@example.com"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("message", "Relation ajoutée avec succès !"))
            .andExpect(view().name("add-relation"));

        // Vérifier que testUser possède désormais relationUser dans sa liste de connexions
        User updatedUser = userRepository.findById(testUser.getId()).get();
        assertTrue(updatedUser.getConnections().contains(relationUser));
    }

    @Test
    void testAddRelation_SelfAddition() throws Exception {
        mockMvc.perform(post("/add-relation")
                .param("email", "testuser@example.com"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("error", containsString("Vous ne pouvez pas vous ajouter comme relation.")))
            .andExpect(view().name("add-relation"));
    }

    @Test
    void testAddRelation_RelationNotFound() throws Exception {
        mockMvc.perform(post("/add-relation")
                .param("email", "nonexistent@example.com"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("error", containsString("User not found")))
            .andExpect(view().name("add-relation"));
    }
}
