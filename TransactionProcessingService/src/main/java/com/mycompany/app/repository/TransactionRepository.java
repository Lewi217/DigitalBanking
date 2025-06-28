package com.mycompany.app.repository;

import com.mycompany.app.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, String> {
    List<Transaction> findByFromAccountIdOrToAccountId(String from, String to);
    List<Transaction> findByCreatedAtBetween(Instant start, Instant end);
    List<Transaction> findByFromAccountIdAndCreatedAtAfter(String fromAccountId, Instant after);
}
