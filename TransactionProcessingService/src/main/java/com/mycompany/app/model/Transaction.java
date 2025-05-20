package com.mycompany.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long fromAccountId;
    private Long toAccountId;
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private TransactionType type;
    private Instant createdAt;
    private Instant scheduledAt;
}
