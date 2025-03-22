package com.paymybuddy.service;

import com.paymybuddy.model.Account;
import com.paymybuddy.model.User;
import com.paymybuddy.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    private User testUser;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Création d'un utilisateur de test
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        // Création d'un compte de test pour cet utilisateur
        testAccount = new Account();
        testAccount.setId(10L);
        testAccount.setOwner(testUser);
        // La balance initiale est de 1000€ (définie dans createAccount)
        testAccount.setBalance(new BigDecimal("1000"));
    }

    @Test
    void testCreateAccount() {
        // Simuler l'appel à accountRepository.save()
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account acc = invocation.getArgument(0);
            acc.setId(10L);
            return acc;
        });

        Account createdAccount = accountService.createAccount(testUser);
        assertNotNull(createdAccount);
        assertEquals(10L, createdAccount.getId());
        assertEquals(new BigDecimal("1000"), createdAccount.getBalance());
        assertEquals(testUser, createdAccount.getOwner());
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void testGetAccountsByUserId() {
        List<Account> accounts = new ArrayList<>();
        accounts.add(testAccount);
        when(accountRepository.findByOwnerId(1L)).thenReturn(accounts);

        List<Account> result = accountService.getAccountsByUserId(1L);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testAccount, result.get(0));
        verify(accountRepository, times(1)).findByOwnerId(1L);
    }

    @Test
    void testGetAccountById_Found() {
        when(accountRepository.findById(10L)).thenReturn(Optional.of(testAccount));
        Optional<Account> result = accountService.getAccountById(10L);
        assertTrue(result.isPresent());
        assertEquals(testAccount, result.get());
        verify(accountRepository, times(1)).findById(10L);
    }

    @Test
    void testGetAccountById_NotFound() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());
        Optional<Account> result = accountService.getAccountById(99L);
        assertFalse(result.isPresent());
        verify(accountRepository, times(1)).findById(99L);
    }

    @Test
    void testUpdateBalance_PositiveAmount() {
        // Simuler un compte existant
        when(accountRepository.findById(10L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Ajouter 200€
        accountService.updateBalance(10L, new BigDecimal("200"));
        // Nouvelle balance doit être 1200€
        assertEquals(new BigDecimal("1200"), testAccount.getBalance());
        verify(accountRepository, times(1)).save(testAccount);
    }

    @Test
    void testUpdateBalance_NegativeAmount() {
        // Simuler un compte existant
        when(accountRepository.findById(10L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Soustraire 300€
        accountService.updateBalance(10L, new BigDecimal("-300"));
        // Nouvelle balance doit être 700€
        assertEquals(new BigDecimal("700"), testAccount.getBalance());
        verify(accountRepository, times(1)).save(testAccount);
    }

    @Test
    void testUpdateBalance_AccountNotFound() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
            accountService.updateBalance(99L, new BigDecimal("100"))
        );
        assertEquals("Account not found", ex.getMessage());
    }

    @Test
    void testDeposit_Success() {
        // Test de dépôt d'argent avec montant positif
        when(accountRepository.findById(10L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        accountService.deposit(10L, new BigDecimal("150"));
        // Nouvelle balance attendue : 1000 + 150 = 1150€
        assertEquals(new BigDecimal("1150"), testAccount.getBalance());
        verify(accountRepository, times(1)).save(testAccount);
    }

    @Test
    void testDeposit_InvalidAmount() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
            accountService.deposit(10L, new BigDecimal("0"))
        );
        assertEquals("Deposit amount must be greater than zero.", ex.getMessage());
    }

    @Test
    void testWithdraw_Success() {
        // Test de retrait d'argent
        when(accountRepository.findById(10L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        accountService.withdraw(10L, new BigDecimal("250"));
        // Nouvelle balance attendue : 1000 - 250 = 750€
        assertEquals(new BigDecimal("750"), testAccount.getBalance());
        verify(accountRepository, times(1)).save(testAccount);
    }

    @Test
    void testWithdraw_InvalidAmount() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
            accountService.withdraw(10L, new BigDecimal("0"))
        );
        assertEquals("Withdrawal amount must be greater than zero.", ex.getMessage());
    }

    @Test
    void testWithdraw_InsufficientBalance() {
        // Définir une balance insuffisante
        testAccount.setBalance(new BigDecimal("100"));
        when(accountRepository.findById(10L)).thenReturn(Optional.of(testAccount));

        Exception ex = assertThrows(IllegalArgumentException.class, () ->
            accountService.withdraw(10L, new BigDecimal("200"))
        );
        assertEquals("Insufficient balance.", ex.getMessage());
    }
}
