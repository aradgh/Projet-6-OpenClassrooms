package com.paymybuddy.repository;

import com.paymybuddy.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findBySenderId(Long senderId);

    List<Transaction> findByReceiverId(Long receiverId);

    List<Transaction> findBySenderIdOrReceiverId(Long senderId, Long receiverId);

    @Query("SELECT t FROM Transaction t WHERE t.sender.id = :userId AND t.timestamp BETWEEN :start AND :end")
    List<Transaction> findSentTransactionsByUserIdAndPeriod(
        @Param("userId") Long userId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end
    );

}

