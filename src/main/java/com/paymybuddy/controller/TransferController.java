package com.paymybuddy.controller;

import com.paymybuddy.model.Account;
import com.paymybuddy.model.Transaction;
import com.paymybuddy.model.User;
import com.paymybuddy.service.AccountService;
import com.paymybuddy.service.TransactionService;
import com.paymybuddy.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class TransferController {

    private final UserService userService;
    private final TransactionService transactionService;
    private final AccountService accountService;

    public TransferController(UserService userService, TransactionService transactionService, AccountService accountService) {
        this.userService = userService;
        this.transactionService = transactionService;
        this.accountService = accountService;
    }

    @GetMapping("/transfer")
    public String showTransferPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User currentUser = userService.getUserByEmail(userDetails.getUsername());
        // Récupère le compte de l'utilisateur (on suppose qu'il n'a qu'un compte)
        List<Account> userAccounts = accountService.getAccountsByUserId(currentUser.getId());
        if(userAccounts.isEmpty()) {
            throw new RuntimeException("L'utilisateur n'a pas de compte.");
        }
        Account currentAccount = userAccounts.get(0);

        List<Transaction> transactions = transactionService.getTransactionsByUser(currentUser.getId());
        List<User> connections = userService.getConnections(currentUser.getId());

        model.addAttribute("transactions", transactions);
        model.addAttribute("connections", connections);
        model.addAttribute("currentAccountId", currentAccount.getId());
        return "transfer";
    }

    @PostMapping("/transfer")
    public String processTransfer(
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestParam("relation") Long receiverId,
        @RequestParam("description") String description,
        @RequestParam("amount") BigDecimal amount
    ) {
        User senderUser = userService.getUserByEmail(userDetails.getUsername());

        // Récupérer le compte de l'expéditeur (on suppose qu'il n'a qu'un compte)
        List<Account> senderAccounts = accountService.getAccountsByUserId(senderUser.getId());
        if (senderAccounts.isEmpty()) {
            throw new RuntimeException("L'expéditeur n'a pas de compte.");
        }
        Account senderAccount = senderAccounts.get(0);

        // Récupérer l'utilisateur destinataire à partir de son ID (celui sélectionné dans le select)
        User receiverUser = userService.getUserById(receiverId)
            .orElseThrow(() -> new RuntimeException("Destinataire introuvable."));

        // Récupérer le compte du destinataire
        List<Account> receiverAccounts = accountService.getAccountsByUserId(receiverUser.getId());
        if (receiverAccounts.isEmpty()) {
            throw new RuntimeException("Le destinataire n'a pas de compte.");
        }
        Account receiverAccount = receiverAccounts.get(0);

        // Créer la transaction (la méthode gère la déduction du solde et la commission)
        transactionService.createTransaction(senderAccount, receiverAccount, amount, description);

        // Rediriger vers la page de transfert pour rafraîchir la liste des transactions
        return "redirect:/transfer";
    }
}
