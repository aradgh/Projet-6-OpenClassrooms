package com.paymybuddy.controller;

import com.paymybuddy.model.Account;
import com.paymybuddy.model.Transaction;
import com.paymybuddy.service.AccountService;
import com.paymybuddy.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final AccountService accountService;

    @Autowired
    public TransactionController(TransactionService transactionService,
                                 AccountService accountService
    ) {
        this.transactionService = transactionService;
        this.accountService = accountService;
    }

    // CREATE - Créer une transaction
    @PostMapping
    public ResponseEntity<Transaction> createTransaction(
        @RequestParam Long senderAccountId,
        @RequestParam Long receiverAccountId,
        @RequestParam BigDecimal amount,
        @RequestParam(required = false) String description
    ) {
        Optional<Account> senderAccount = accountService.getAccountById(senderAccountId);
        Optional<Account> receiverAccount = accountService.getAccountById(receiverAccountId);

        if (senderAccount.isPresent() && receiverAccount.isPresent()) {
            Transaction transaction = transactionService.createTransaction(
                senderAccount.get(), receiverAccount.get(), amount, description);
            return ResponseEntity.ok(transaction);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // READ - Récupérer les transactions envoyées par un utilisateur
    @GetMapping("/sent/{senderId}")
    public ResponseEntity<List<Transaction>> getSentTransactions(@PathVariable Long senderId) {
        List<Transaction> transactions = transactionService.getTransactionsBySenderId(senderId);
        return ResponseEntity.ok(transactions);
    }

    // READ - Récupérer les transactions reçues par un utilisateur
    @GetMapping("/received/{receiverId}")
    public ResponseEntity<List<Transaction>> getReceivedTransactions(@PathVariable Long receiverId) {
        List<Transaction> transactions = transactionService.getTransactionsByReceiverId(receiverId);
        return ResponseEntity.ok(transactions);
    }
}
