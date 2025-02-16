package com.paymybuddy.service;

import com.paymybuddy.model.Account;
import com.paymybuddy.model.Transaction;
import com.paymybuddy.model.User;
import com.paymybuddy.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class TransactionService {

    private static final BigDecimal COMMISSION_RATE = new BigDecimal("0.005"); // 0,5%
    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    Logger logger = Logger.getLogger(getClass().getName());

    @Autowired
    public TransactionService(TransactionRepository transactionRepository, AccountService accountService) {
        this.transactionRepository = transactionRepository;
        this.accountService = accountService;
    }

    //    @Transactional
    // Tester avec ou sans l'annotation pour voir le comportement
    public Transaction createTransaction(User sender, User receiver, BigDecimal amount, String description) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }

        // Calculer la commission
        BigDecimal commission = amount.multiply(COMMISSION_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalDeduction = amount.add(commission); // Montant total déduit de l'expéditeur

        // Déduire le montant du compte de l'expéditeur
        List<Account> senderAccounts = accountService.getAccountsByUserId(sender.getId());
        if (senderAccounts.isEmpty() || senderAccounts.get(0).getBalance().compareTo(totalDeduction) < 0) {
            throw new IllegalArgumentException("Insufficient balance.");
        }
        accountService.updateBalance(senderAccounts.get(0).getId(), totalDeduction.negate());

        // Ajouter le montant au compte du destinataire
        List<Account> receiverAccounts = accountService.getAccountsByUserId(receiver.getId());
        if (receiverAccounts.isEmpty()) {
            throw new IllegalArgumentException("Receiver has no account.");
        }
        accountService.updateBalance(receiverAccounts.get(0).getId(), amount);

        // Créer et sauvegarder la transaction
        Transaction transaction = new Transaction();
        transaction.setSender(sender);
        transaction.setReceiver(receiver);
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setTimestamp(LocalDateTime.now());
        transactionRepository.save(transaction);

        logger.log(Level.INFO, "Transaction created. Commission charged: {}", commission);

        return transaction;
    }

    public List<Transaction> getTransactionsBySenderId(Long senderId) {
        return transactionRepository.findBySenderId(senderId);
    }

    public List<Transaction> getTransactionsByReceiverId(Long receiverId) {
        return transactionRepository.findByReceiverId(receiverId);
    }
}
