package com.paymybuddy.repository;

import com.paymybuddy.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    // Trouver tous les comptes appartenant à un utilisateur
    List<Account> findByOwnerId(Long ownerId);
}
