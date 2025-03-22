package com.paymybuddy.repository;

import com.paymybuddy.model.Account;
import com.paymybuddy.model.Transaction;
import com.paymybuddy.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class TransactionRepositoryIntegrationTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testFindBySenderIdOrReceiverId() {
        // Création de deux utilisateurs
        User user1 = new User();
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");
        user1.setPassword("secret");
        user1 = userRepository.save(user1);

        User user2 = new User();
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setPassword("secret");
        user2 = userRepository.save(user2);

        // Création des comptes correspondants
        Account account1 = new Account();
        account1.setOwner(user1);
        account1.setBalance(new BigDecimal("1000"));
        account1 = accountRepository.save(account1);

        Account account2 = new Account();
        account2.setOwner(user2);
        account2.setBalance(new BigDecimal("1000"));
        account2 = accountRepository.save(account2);

        // Création d'une transaction : user1 envoie de l'argent à user2
        Transaction transaction = new Transaction();
        transaction.setSender(account1);
        transaction.setReceiver(account2);
        transaction.setAmount(new BigDecimal("100"));
        transaction.setDescription("Payment");
        transaction.setTimestamp(LocalDateTime.now());
        transactionRepository.save(transaction);

        List<Transaction> sent = transactionRepository.findBySenderId(account1.getId());
        List<Transaction> received = transactionRepository.findByReceiverId(account2.getId());
        List<Transaction> both = transactionRepository.findBySenderIdOrReceiverId(account1.getId(), account1.getId());

        assertFalse(sent.isEmpty());
        assertFalse(received.isEmpty());
        assertFalse(both.isEmpty());
    }
}
