package com.paymybuddy.service;

import com.paymybuddy.model.Account;
import com.paymybuddy.model.Transaction;
import com.paymybuddy.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private TransactionService transactionService;

    private Account senderAccount;
    private Account receiverAccount;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        // Simuler un compte expéditeur avec un "user id" égal à 1 (pour simplifier)
        senderAccount = new Account();
        senderAccount.setId(1L);
        senderAccount.setBalance(new BigDecimal("1000"));
        // Simuler un compte destinataire avec un "user id" égal à 2
        receiverAccount = new Account();
        receiverAccount.setId(2L);
        receiverAccount.setBalance(new BigDecimal("500"));
    }

    @Test
    void testCreateTransaction_Success() {
        BigDecimal amount = new BigDecimal("100");
        BigDecimal commission = amount.multiply(new BigDecimal("0.005")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalDeduction = amount.add(commission);

        // Simuler que l'expéditeur et le destinataire possèdent chacun un compte
        when(accountService.getAccountsByUserId(senderAccount.getId()))
            .thenReturn(Collections.singletonList(senderAccount));
        when(accountService.getAccountsByUserId(receiverAccount.getId()))
            .thenReturn(Collections.singletonList(receiverAccount));

        // Simuler la sauvegarde de la transaction pour lui assigner un ID
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setId(10L);
            return t;
        });

        String description = "Test transaction";
        Transaction result = transactionService.createTransaction(senderAccount, receiverAccount, amount, description);

        // Vérifier que le solde du compte de l'expéditeur est diminué du montant total
        verify(accountService).updateBalance(senderAccount.getId(), totalDeduction.negate());
        // Vérifier que le solde du compte du destinataire est augmenté du montant
        verify(accountService).updateBalance(receiverAccount.getId(), amount);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals(description, result.getDescription());
        assertEquals(amount, result.getAmount());
        assertNotNull(result.getTimestamp());
        assertEquals(senderAccount, result.getSender());
        assertEquals(receiverAccount, result.getReceiver());
    }

    @Test
    void testCreateTransaction_InvalidAmount() {
        BigDecimal invalidAmount = BigDecimal.ZERO;
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
            transactionService.createTransaction(senderAccount, receiverAccount, invalidAmount, "Test")
        );
        assertEquals("Amount must be greater than zero.", ex.getMessage());
    }

    @Test
    void testCreateTransaction_InsufficientBalance() {
        BigDecimal amount = new BigDecimal("100");

        // Simuler un solde insuffisant sur le compte de l'expéditeur
        senderAccount.setBalance(new BigDecimal("50"));

        when(accountService.getAccountsByUserId(senderAccount.getId()))
            .thenReturn(Collections.singletonList(senderAccount));

        Exception ex = assertThrows(IllegalArgumentException.class, () ->
            transactionService.createTransaction(senderAccount, receiverAccount, amount, "Test")
        );
        assertEquals("Insufficient balance.", ex.getMessage());
    }

    @Test
    void testCreateTransaction_ReceiverNoAccount() {
        BigDecimal amount = new BigDecimal("100");

        when(accountService.getAccountsByUserId(senderAccount.getId()))
            .thenReturn(Collections.singletonList(senderAccount));
        when(accountService.getAccountsByUserId(receiverAccount.getId()))
            .thenReturn(Collections.emptyList());

        Exception ex = assertThrows(IllegalArgumentException.class, () ->
            transactionService.createTransaction(senderAccount, receiverAccount, amount, "Test")
        );
        assertEquals("Receiver has no account.", ex.getMessage());
    }

    @Test
    void testGetTransactionsBySenderId() {
        Long senderId = 1L;
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction());
        when(transactionRepository.findBySenderId(senderId)).thenReturn(transactions);

        List<Transaction> result = transactionService.getTransactionsBySenderId(senderId);
        assertEquals(transactions.size(), result.size());
    }

    @Test
    void testGetTransactionsByReceiverId() {
        Long receiverId = 2L;
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction());
        when(transactionRepository.findByReceiverId(receiverId)).thenReturn(transactions);

        List<Transaction> result = transactionService.getTransactionsByReceiverId(receiverId);
        assertEquals(transactions.size(), result.size());
    }

    @Test
    void testGetTransactionsByUser() {
        Long userId = 1L;
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction());
        when(transactionRepository.findBySenderIdOrReceiverId(userId, userId))
            .thenReturn(transactions);

        List<Transaction> result = transactionService.getTransactionsByUser(userId);
        assertEquals(transactions.size(), result.size());
    }
}
