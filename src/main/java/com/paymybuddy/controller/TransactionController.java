package com.paymybuddy.controller;

import com.paymybuddy.model.Transaction;
import com.paymybuddy.model.User;
import com.paymybuddy.service.TransactionService;
import com.paymybuddy.service.UserService;
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
    private final UserService userService;

    @Autowired
    public TransactionController(TransactionService transactionService, UserService userService) {
        this.transactionService = transactionService;
        this.userService = userService;
    }

    // CREATE - Créer une transaction
    @PostMapping
    public ResponseEntity<Transaction> createTransaction(
        @RequestParam Long senderId, @RequestParam Long receiverId, @RequestParam BigDecimal amount,
        @RequestParam(required = false) String description
    ) {

        Optional<User> sender = userService.getUserById(senderId);
        Optional<User> receiver = userService.getUserById(receiverId);

        if (sender.isPresent() && receiver.isPresent()) {
            Transaction transaction = transactionService.createTransaction(
                sender.get(), receiver.get(), amount, description);
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
