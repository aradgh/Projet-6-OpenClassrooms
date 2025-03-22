package com.paymybuddy.repository;

import com.paymybuddy.model.Account;
import com.paymybuddy.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class AccountRepositoryIntegrationTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testFindByOwnerId() {
        // Création d'un utilisateur
        User user = new User();
        user.setUsername("alice");
        user.setEmail("alice@example.com");
        user.setPassword("secret");
        user = userRepository.save(user);

        // Création de deux comptes pour cet utilisateur
        Account account1 = new Account();
        account1.setOwner(user);
        account1.setBalance(new BigDecimal("1000"));
        accountRepository.save(account1);

        Account account2 = new Account();
        account2.setOwner(user);
        account2.setBalance(new BigDecimal("2000"));
        accountRepository.save(account2);

        List<Account> accounts = accountRepository.findByOwnerId(user.getId());
        assertEquals(2, accounts.size());
    }
}
