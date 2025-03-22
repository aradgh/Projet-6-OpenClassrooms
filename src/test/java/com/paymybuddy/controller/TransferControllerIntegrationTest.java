package com.paymybuddy.controller;

import com.paymybuddy.model.Account;
import com.paymybuddy.model.User;
import com.paymybuddy.repository.AccountRepository;
import com.paymybuddy.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "testuser@example.com", roles = {"USER"})
class TransferControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    User testUser;
    Account testUserAccount;

    User connectionUser;
    Account connectionUserAccount;

    @BeforeEach
    void setup() {
        // Nettoyage des tables
        accountRepository.deleteAll();
        userRepository.deleteAll();

        // Création de l'utilisateur connecté
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("testuser@example.com");
        testUser.setPassword("password"); // Dans un test, le hash n'est pas nécessaire
        testUser = userRepository.save(testUser);

        testUserAccount = new Account();
        testUserAccount.setOwner(testUser);
        testUserAccount.setBalance(new BigDecimal("1000"));
        testUserAccount = accountRepository.save(testUserAccount);

        // Création d'un utilisateur de connexion (destinataire)
        connectionUser = new User();
        connectionUser.setUsername("connection");
        connectionUser.setEmail("connection@example.com");
        connectionUser.setPassword("password");
        connectionUser = userRepository.save(connectionUser);

        connectionUserAccount = new Account();
        connectionUserAccount.setOwner(connectionUser);
        connectionUserAccount.setBalance(new BigDecimal("1000"));
        connectionUserAccount = accountRepository.save(connectionUserAccount);

        // Optionnel : Ajouter la connexion dans les relations de testUser
        // (selon la logique de votre application, par exemple via userService.addConnection(...))
    }

    @Test
    void testShowTransferPage() throws Exception {
        mockMvc.perform(get("/transfer"))
            .andExpect(status().isOk())
            .andExpect(view().name("transfer"))
            .andExpect(model().attribute("transactions", hasSize(0)))
            .andExpect(model().attribute("connections", notNullValue()))
            .andExpect(model().attribute("currentAccountId", is(3L)));
    }

    @Test
    void testProcessTransfer_Success() throws Exception {
        // On effectue un transfert depuis testUserAccount vers le compte de connectionUser
        BigDecimal amount = new BigDecimal("100");
        String description = "Test transfer";

        mockMvc.perform(post("/transfer")
                .param("relation", connectionUser.getId().toString())
                .param("amount", amount.toPlainString())
                .param("description", description))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/transfer"));

        // Vérifier que le solde du compte expéditeur a diminué et celui du destinataire augmenté
        Account updatedSender = accountRepository.findById(testUserAccount.getId()).orElseThrow();
        Account updatedReceiver = accountRepository.findById(connectionUserAccount.getId()).orElseThrow();

        // Calcul de la commission (0,5% de 100) => 0,50, total déduit = 100.50
        BigDecimal commission = amount.multiply(new BigDecimal("0.005")).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal totalDeduction = amount.add(commission);

        assertEquals(new BigDecimal("1000").subtract(totalDeduction), updatedSender.getBalance());
        assertEquals(new BigDecimal("1000.00").add(amount), updatedReceiver.getBalance());
    }
}
