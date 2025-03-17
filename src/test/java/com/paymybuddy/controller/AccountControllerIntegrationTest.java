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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "testuser", roles = {"USER"})
class AccountControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    private User testUser;

    @BeforeEach
    public void setup() {
        // Nettoyer les tables pour chaque test
        accountRepository.deleteAll();
        userRepository.deleteAll();

        // Créer un utilisateur de test
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("testuser@example.com");
        testUser.setPassword("password"); // Dans un test, ce n'est pas crucial (normalement il sera haché)
        testUser = userRepository.save(testUser);
    }

    @Test
    void testCreateAccount() throws Exception {
        // Appel POST pour créer un compte pour l'utilisateur de test via /api/accounts/{userId}
        mockMvc.perform(post("/api/accounts/{userId}", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.balance", is(1000))) // Le solde initial doit être 1000€
            .andExpect(jsonPath("$.owner.id", is(testUser.getId().intValue())));
    }

    @Test
    void testGetAccountsByUserId() throws Exception {
        // Créer un compte pour testUser
        Account account = new Account();
        account.setOwner(testUser);
        account.setBalance(new BigDecimal("1000"));
        account = accountRepository.save(account);

        // Appel GET sur /api/accounts/{userId} et vérification du contenu JSON
        mockMvc.perform(get("/api/accounts/{userId}", testUser.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id", is(account.getId().intValue())))
            .andExpect(jsonPath("$[0].balance", is(1000.0)));
    }

    @Test
    void testUpdateBalance() throws Exception {
        // Créer un compte pour testUser
        Account account = new Account();
        account.setOwner(testUser);
        account.setBalance(new BigDecimal("1000"));
        account = accountRepository.save(account);

        // Appel PUT pour mettre à jour la balance en ajoutant 200€
        mockMvc.perform(put("/api/accounts/{accountId}", account.getId())
                .param("amount", "200"))
            .andExpect(status().isNoContent());

        Account updatedAccount = accountRepository.findById(account.getId()).get();
        // 1000 + 200 = 1200
        assertTrue(updatedAccount.getBalance().compareTo(new BigDecimal("1200")) == 0);
    }

    @Test
    void testDeposit() throws Exception {
        // Créer un compte pour testUser
        Account account = new Account();
        account.setOwner(testUser);
        account.setBalance(new BigDecimal("1000"));
        account = accountRepository.save(account);

        // Appel POST pour déposer 150€
        mockMvc.perform(post("/api/accounts/{accountId}/deposit", account.getId())
                .param("amount", "150"))
            .andExpect(status().isOk())
            .andExpect(content().string("Deposit successful."));

        Account updatedAccount = accountRepository.findById(account.getId()).get();
        // 1000 + 150 = 1150
        assertTrue(updatedAccount.getBalance().compareTo(new BigDecimal("1150")) == 0);
    }

    @Test
    void testWithdraw() throws Exception {
        // Créer un compte pour testUser
        Account account = new Account();
        account.setOwner(testUser);
        account.setBalance(new BigDecimal("1000"));
        account = accountRepository.save(account);

        // Appel POST pour retirer 250€
        mockMvc.perform(post("/api/accounts/{accountId}/withdraw", account.getId())
                .param("amount", "250"))
            .andExpect(status().isOk())
            .andExpect(content().string("Withdrawal successful."));

        Account updatedAccount = accountRepository.findById(account.getId()).get();
        // 1000 - 250 = 750
        assertTrue(updatedAccount.getBalance().compareTo(new BigDecimal("750")) == 0);
    }
}
