package com.paymybuddy.controller;

import com.paymybuddy.model.Account;
import com.paymybuddy.model.User;
import com.paymybuddy.service.AccountService;
import com.paymybuddy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;
    private final UserService userService;

    @Autowired
    public AccountController(AccountService accountService, UserService userService) {
        this.accountService = accountService;
        this.userService = userService;
    }

    @PostMapping("/{userId}")
    public ResponseEntity<Account> createAccount(@PathVariable Long userId) {
        Optional<User> user = userService.getUserById(userId);
        if (user.isPresent()) {
            Account account = accountService.createAccount(user.get());
            return ResponseEntity.ok(account);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<Account>> getAccountsByUserId(@PathVariable Long userId) {
        List<Account> accounts = accountService.getAccountsByUserId(userId);
        return ResponseEntity.ok(accounts);
    }

    @PutMapping("/{accountId}")
    public ResponseEntity<Void> updateBalance(@PathVariable Long accountId, @RequestParam BigDecimal amount) {
        accountService.updateBalance(accountId, amount);
        return ResponseEntity.noContent().build();
    }

    // Dépôt d'argent
    @PostMapping("/{accountId}/deposit")
    public ResponseEntity<String> deposit(@PathVariable Long accountId, @RequestParam BigDecimal amount) {
        accountService.deposit(accountId, amount);
        return ResponseEntity.ok("Deposit successful.");
    }

    // Retrait d'argent
    @PostMapping("/{accountId}/withdraw")
    public ResponseEntity<String> withdraw(@PathVariable Long accountId, @RequestParam BigDecimal amount) {
        accountService.withdraw(accountId, amount);
        return ResponseEntity.ok("Withdrawal successful.");
    }
}
