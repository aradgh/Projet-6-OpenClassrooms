package com.paymybuddy.controller;

import com.paymybuddy.model.Account;
import com.paymybuddy.model.Transaction;
import com.paymybuddy.model.User;
import com.paymybuddy.repository.AccountRepository;
import com.paymybuddy.repository.TransactionRepository;
import com.paymybuddy.repository.UserRepository;
import com.paymybuddy.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "testuser@example.com", roles = "USER")
class TransactionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionService transactionService;

    Account senderAccount;
    Account receiverAccount;

    @BeforeEach
    void setup() {
        // Nettoyage des tables
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        userRepository.deleteAll();

        // Création d'un utilisateur émetteur
        User sender = new User();
        sender.setUsername("sender");
        sender.setEmail("sender@example.com");
        sender.setPassword("password");
        sender = userRepository.save(sender);

        // Création d'un utilisateur destinataire
        User receiver = new User();
        receiver.setUsername("receiver");
        receiver.setEmail("receiver@example.com");
        receiver.setPassword("password");
        receiver = userRepository.save(receiver);

        // Création des comptes avec un solde initial de 1000€
        senderAccount = new Account();
        senderAccount.setOwner(sender);
        senderAccount.setBalance(new BigDecimal("1000"));
        senderAccount = accountRepository.save(senderAccount);

        receiverAccount = new Account();
        receiverAccount.setOwner(receiver);
        receiverAccount.setBalance(new BigDecimal("1000"));
        receiverAccount = accountRepository.save(receiverAccount);
    }

    @Test
    void testCreateTransaction_Success() throws Exception {
        BigDecimal amount = new BigDecimal("100");
        String description = "Payment test";

        mockMvc.perform(post("/api/transactions")
                .param("senderAccountId", String.valueOf(senderAccount.getId()))
                .param("receiverAccountId", String.valueOf(receiverAccount.getId()))
                .param("amount", amount.toPlainString())
                .param("description", description))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", notNullValue()))
            .andExpect(jsonPath("$.amount", is(amount.intValue()))) // Pour simplifier, ici on compare en int
            .andExpect(jsonPath("$.description", is(description)));

        // Calcul de la commission (0,5%)
        BigDecimal commission = amount.multiply(new BigDecimal("0.005")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalDeduction = amount.add(commission);

        Account updatedSender = accountRepository.findById(senderAccount.getId()).orElseThrow();
        Account updatedReceiver = accountRepository.findById(receiverAccount.getId()).orElseThrow();

        assertEquals(new BigDecimal("1000").subtract(totalDeduction), updatedSender.getBalance());
        assertEquals(new BigDecimal("1000.00").add(amount), updatedReceiver.getBalance());
    }

    @Test
    void testGetSentTransactions() throws Exception {
        // Création d'une transaction envoyée par senderAccount
        Transaction transaction = transactionService.createTransaction(senderAccount, receiverAccount, new BigDecimal("50"), "Sent test");

        // On simule une requête GET pour récupérer les transactions envoyées par l'utilisateur émetteur
        Long senderId = senderAccount.getOwner().getId();
        mockMvc.perform(get("/api/transactions/sent/{senderId}", senderId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id", is(transaction.getId().intValue())))
            .andExpect(jsonPath("$[0].description", is("Sent test")));
    }

    @Test
    void testGetReceivedTransactions() throws Exception {
        // Création d'une transaction reçue par receiverAccount
        Transaction transaction = transactionService.createTransaction(senderAccount, receiverAccount, new BigDecimal("75"), "Received test");

        // On simule une requête GET pour récupérer les transactions reçues par l'utilisateur destinataire
        Long receiverId = receiverAccount.getOwner().getId();
        mockMvc.perform(get("/api/transactions/received/{receiverId}", receiverId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id", is(transaction.getId().intValue())))
            .andExpect(jsonPath("$[0].description", is("Received test")));
    }
}
